package com.aternity.tupleBuilder.controller;

import com.aternity.agentSimulator.simulator.commManager.http.HttpCommManager;
import com.aternity.tupleBuilder.ArcTuple.ArcTupleMessage;
import com.aternity.tupleBuilder.mobile.MobileDeviceAttributes;
import com.aternity.tupleBuilder.mobile.MobileDeviceResources;
import com.aternity.tupleBuilder.mobile.MobileMonitors;
import com.aternity.tupleBuilder.utils.DBUtil;
import com.aternity.tupleBuilder.utils.HttpUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Controller
public class ArcTupleBuilder {

    static {
        HttpCommManager.initLog("");
        HttpCommManager.simulatorOnSingleRequestMode = true;
    }

    @RequestMapping(value = "/genericSender")
    public String genericSender(@RequestParam(name="urlPrefix", required=false) String urlPrefix,
                                 @RequestParam(name="epId", required=false) String epId, Model model)  throws Exception {
        String fullUrl = urlPrefix + "/Httpagent/Report?configId=-1&epId=" + epId + "&enabledMonitorsConfigId=0";
        model.addAttribute("urlPrefix",urlPrefix);
        model.addAttribute("epId",epId);
        model.addAttribute("fullUrl",fullUrl);
        return "genericDataSender";
    }

    @RequestMapping(value = "/editBeforeSend")
    public String editBeforeSend(@RequestParam(name="accountName", required=false) String accountName,
                         @RequestParam(name="virtualEpId", required=false) String virtualEpId,
                         @RequestParam(name="url", required=false) String url,
                         @RequestParam(name="isMac", required=false) String isMac,
                         @RequestParam(name="isAppInternalUserTransactions", required=false) String isAppInternalUserTransactions,
                         @RequestParam(name="data", required=false) String data,
                         @RequestParam(name="epmIP", required=false) String epmIP,
                         @RequestParam(name="dbUser", required=false) String dbUser,
                         @RequestParam(name="dbPassword", required=false) String dbPassword,
                         @RequestParam(name="globalEpId", required=false) String globalEpId,
                         @RequestParam(name="appInternalSection", required=false) boolean appInternalSection,
                         @RequestParam(name="isWifi", required=false) boolean isWifi, Model model)  throws Exception{

        data = data.replaceAll("\\*\\*\\*","[VALUE]");
        data = getTransformedTuple(epmIP, dbUser, dbPassword, data, globalEpId, virtualEpId, accountName, appInternalSection, isWifi);
        model.addAttribute("data",data);
        model.addAttribute("epmIP",epmIP);
        model.addAttribute("isMac",isMac);
        model.addAttribute("url",url);
        model.addAttribute("dbUser",dbUser);
        model.addAttribute("dbPassword",dbPassword);
        model.addAttribute("globalEpId",globalEpId);
        model.addAttribute("accountName",accountName);
        return "editBeforeSend";
    }

    @RequestMapping(value = "/builder")
    public String index(@RequestParam(name="epmIP", required=false) String epmIP,
                             @RequestParam(name="EPMOnOtherMachineVal", required=false) String EPMOnOtherMachineVal,
                             @RequestParam(name="dbUser", required=false) String dbUser,
                             @RequestParam(name="accountID", required=false) String accountID,
                             @RequestParam(name="dbPassword", required=false) String dbPassword,
                             @RequestParam(name="config", required=false) String config,
                             @RequestParam(name="testConnection", required=false) String testConnection, Model model)  {

        if (epmIP != null && (EPMOnOtherMachineVal == null || EPMOnOtherMachineVal.length() < 1)) {
            EPMOnOtherMachineVal = epmIP.split(":")[0] + ":80";
        }
        String epmForAgent = epmIP != null ? epmIP.split(":")[0] + ":80" : "";
        accountID = accountID != null ? accountID : "0";
        List<String> appTypes = new ArrayList<String>() {
        };
        List<String> accountNames = new ArrayList<String>() {
        };

        LinkedHashMap<String, String> staticNames = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> MobileAttributes = new LinkedHashMap<String, String>();
        List<String> MobileMonitorsMap = new ArrayList<String>();
        List<String> DeviceResourcesMap = new ArrayList<String>();
        if (epmIP == null && dbUser == null && dbPassword == null){
            model.addAttribute("error","Please set DB connection");
        }
        try {

            MobileAttributes = MobileDeviceAttributes.getMobileAttributesForIndex(epmIP, dbUser, dbPassword);
            DeviceResourcesMap = MobileDeviceResources.getDeviceResources();
            MobileMonitorsMap = MobileMonitors.getMonitors(epmIP, dbUser, dbPassword);
        } catch (Exception e) {

        }
        String allStatics = "AD Department=Management,AD Office=Israel Office,Agent Current Status=Reporting,Agent Identification=AG9.1,Agent Version=0.0.5.240,Aggregation Server=EPM1 Default alias,CPU Cores=8,CPU Speed=3GHz to 4GHz,Country=Israel,Department=Management,Email Address=Tuple.Builder@automation.com,Enforce Privacy=false,Manufacturer=Dell,Memory=4,Model=Del LatitudeNetwork in Use=0,OS Service Pack=Microsoft Windows 10 Enterprise SP0.0,OS Type=Microsoft Windows 10,Office=Hod hasharon Office,On Site=true,On VPN=false,Operating System=Microsoft Windows 10 Enterprise,Reliability Grade=7.3,Site Location=Israel/Hod hasharon,Site Name=Hod hasharon,State=Israel,Subnet=172.0.0/16";
        String defaultMobileAttr = "Network in use=WiFi,Geographic format=53.347800 -6.259700,Mobile Carrier=Cellcom";
        String test = "";
        String error = "Database settings OK.";
        boolean dbConnection = true;

        try {
            appTypes = Arrays.asList(getMonitorsType(epmIP, dbUser, dbPassword).split(","));
            if (appTypes.size() == 1 && (appTypes.get(0).equalsIgnoreCase("na"))){
                dbConnection = false;
                error = "<br><font color='red'>SQL Exception: Check query => select distinct MONITOR_TYPE  from monitor  order by 1</font>";
            }
            accountNames = Arrays.asList(getAccounts(epmIP, dbUser, dbPassword).split(","));
            if (accountNames.size() == 1 && (accountNames.get(0).equalsIgnoreCase("na"))){
                dbConnection = false;
                error = "SQL Exception: Check query => select * from account";
            }
            staticNames = getStatics(epmIP, dbUser, dbPassword);
        } catch (Exception e) {
            dbConnection = false;
            error = "SQL Exception:" + e.getMessage()
                    + " (Check your User/Password or if DB is up !)";

        }
        if (dbConnection && appTypes.size() == 0) {
            error = "No Monitors found ! Check Server and Database !!!";
        }
        if (!testEPMURLConnection(EPMOnOtherMachineVal)) {
            error += "Failed to connect to " + EPMOnOtherMachineVal + " !!!";
        } else {
            error += "EPM URL " + EPMOnOtherMachineVal + " OK";
        }
        HashMap<String,String> cmConfigs = new HashMap<>();
        TreeMap<String, String> sortedCMConfigs = new TreeMap<>();
        try {
            String cmResponse = HttpUtil.getContentFromUrl("http://configuration-manager/api/osp/configurations").replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");
            for (String configFromCM  :cmResponse.split(",")){
                cmConfigs.put(configFromCM.split(":")[0],configFromCM.split(":",2)[1]);
            }
            sortedCMConfigs.putAll(cmConfigs);
        } catch (Exception e){
            System.out.println(e.toString());
        }
        model.addAttribute("epmIP", epmIP);
        model.addAttribute("appTypes", appTypes);
        model.addAttribute("epmIP", epmIP);
        model.addAttribute("epmForAgent", epmForAgent);
        model.addAttribute("dbUser", dbUser);
        model.addAttribute("accountNames", accountNames);
        model.addAttribute("dbPassword", dbPassword);
        model.addAttribute("staticNames", staticNames);
        model.addAttribute("MobileAttributes", MobileAttributes);
        model.addAttribute("DeviceResourcesMap", DeviceResourcesMap);
        model.addAttribute("MobileMonitorsMap", MobileMonitorsMap);
        model.addAttribute("accountNames", accountNames);
        model.addAttribute("allStatics", allStatics);
        model.addAttribute("error", error);
        model.addAttribute("config", config);
        model.addAttribute("sortedCMConfigs", sortedCMConfigs);
    return "tupleBuilderPage";
    }

    //////////////////////////////////////////////////////////////////////////////////////

    public static String getTransformedTuple(String epmIP, String userDB, String passwordDB, String data,
                                              String globalEpId, String virtualEpId, String accountName, boolean appInternalSection, boolean wifiSection) throws Exception {
        Connection conn = DBUtil.getDBConnection(epmIP, userDB, passwordDB);
        String arcMessageFile = ArcTupleMessage.generateArcTupple(conn, data, globalEpId, virtualEpId, accountName, appInternalSection, wifiSection);
        if (arcMessageFile.startsWith("Got exception")) {
            return arcMessageFile;
        } else
            return (FileUtils.readFileToString(new File(arcMessageFile), "UTF-8"));
    }

    private static boolean testEPMURLConnection(String url) {
        try {

            if (url!= null && url.contains(":80")) {
                url = "http://" + url;
                URL urlToTest = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urlToTest.openConnection();
                if (con.getResponseCode() == 302 && con.getHeaderField("Location").startsWith("https")) {
                    return false;
                }
            }
            if (url!= null && url.contains(":443")) {
                url = "https://" + url;
                URL urlToTest = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) urlToTest.openConnection();
                return true;
            }
            return true;
        } catch (MalformedURLException e) {
            System.out.println(" Test Connection - MalformedURLException");
            return false;
        } catch (IOException e) {
            System.out.println(" Test Connection - IOException");
            return false;
        }
    }
    public static LinkedHashMap<String, String> getStatics(String epmIP, String userDB, String passwordDB)
            throws Exception {
        LinkedHashMap<String, String> response = new LinkedHashMap<String, String>();
        Statement statement = DBUtil.getStatement(epmIP, userDB, passwordDB);
        String query = " select  id, name  from static_attr  where contextual = 0  and name not like '%deleted%' order by 2 ";

        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            response.put(result.getString("name"), result.getString("id"));
        }
        //Gilad : 28/5/2019 MAC and IP attrs are stored in Cassandra and have hard-coded IDs.
        response.put("List of MAC addresses","-1000");
        response.put("List of IP addresses","-1001");
        return response;
    }

    public static String getMonitorsType(String epmIP, String userDB, String passwordDB) throws Exception {
        String response = "";
        Statement statement = DBUtil.getStatement(epmIP, userDB, passwordDB);
        String query = "select distinct MONITOR_TYPE  from monitor  order by 1";

        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            if (!result.getString("MONITOR_TYPE").startsWith("Mobile")) {
                response = response + result.getString("MONITOR_TYPE") + ",";
            }
        }
        if (response.length()== 0){
            return "NA";
        } else {
            return response.substring(0, response.length() - 1);
        }
    }



    public static String getAccounts(String epmIP, String userDB, String passwordDB) throws Exception {
        String response = "";
        Statement statement = DBUtil.getStatement(epmIP, userDB, passwordDB);
        String query = "select * from account where account_id >= 0 and enabled=1 order by 1";

        boolean isNonSaasServer = true;
        ;
        ResultSet resultNonSaas = statement.executeQuery("select * from account where account_id = 0");
        while (resultNonSaas.next()) {
            if (resultNonSaas.getString("COMPANY_NAME").equalsIgnoreCase("default account")) {
                isNonSaasServer = false;
                break;
            }
        }

        if (isNonSaasServer) {
            return "----------";
        }
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            response = response + result.getString("COMPANY_NAME") + ",";
        }
        if (response.length()== 0){
            return "NA";
        } else {
            return response.substring(0, response.length() - 1);
        }
    }

    public static String getAccountByName(String epmIP, String userDB, String passwordDB, String accountName,
                                          String retreiveValue) throws Exception {
        if (accountName == null && retreiveValue.contains("ID")) {
            return "0";
        }
        if (accountName.contains("Default account") && retreiveValue.contains("ID")) {
            return "0";
        }
        if (accountName.contains("----")) {
            return "-1";
        }
        if (accountName.contains("Default account") && retreiveValue.contains("ACCOUNT_KEY")) {
            return null;
        }
        String response = "";

        Statement statement = DBUtil.getStatement(epmIP, userDB, passwordDB);
        String query = "select * from account where COMPANY_NAME='" + accountName + "'";

        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            response = response + result.getString(retreiveValue) + ",";
        }
        return response.substring(0, response.length() - 1);
    }


}

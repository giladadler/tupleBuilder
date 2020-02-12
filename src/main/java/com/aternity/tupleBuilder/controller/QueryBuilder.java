package com.aternity.tupleBuilder.controller;

import com.aternity.agentSimulator.simulator.Configuration;
import com.aternity.agentSimulator.simulator.commManager.http.HttpCommManager;
import com.aternity.agentSimulator.simulator.commManager.http.SslHelper;
import com.aternity.agentSimulator.simulator.messages.incomingMessages.IncomingPathIdMessage;
import com.aternity.agentSimulator.simulator.messages.outgoingMessages.OutgoingSessionUpdateHttpMessage;
import com.aternity.tupleBuilder.ArcTuple.HttpComm;
import com.aternity.tupleBuilder.mobile.MobileBuilder;
import com.aternity.tupleBuilder.mobile.MobileDeviceAttributes;
import com.aternity.tupleBuilder.mobile.MobileMonitors;
import com.aternity.tupleBuilder.utils.DBUtil;
import com.aternity.tupleBuilder.utils.RandUtil;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.*;

import static com.aternity.tupleBuilder.controller.ArcTupleBuilder.getAccountByName;


@RestController
public class QueryBuilder {

    static {
        HttpCommManager.simulatorOnSingleRequestMode = true;
    }

    @RequestMapping("/generateEP")
    public static String generateEP(@RequestParam(name="device", required=false) String device,
                                    @RequestParam(name="EPIP", required=false) String EPIP,
                                    @RequestParam(name="epmIP", required=false) String epmIP,
                                    @RequestParam(name="dbUser", required=false) String dbUser,
                                    @RequestParam(name="accountName", required=false) String accountName,
                                    @RequestParam(name="deviceModelString", required=false) String deviceModelString,
                                    @RequestParam(name="multipleUsersPerSession", required=false) String multipleUsersPerSession,
                                    @RequestParam(name="agentVersion", required=false) String agentVersion,
                                    @RequestParam(name="url", required=false) String url,
                                    @RequestParam(name="hostname", required=false) String hostname,
                                    @RequestParam(name="numberOfSession", required=false) String numberOfSession,
                                    @RequestParam(name="dbPassword", required=false) String dbPassword) throws Exception {

        Boolean vdi = false;
        if (device.toLowerCase().startsWith("virtual desktop")) {
            vdi = true;
            device = "Desktop";
        }

        if (agentVersion.contains("undefin") || agentVersion.contains("null")) {
            agentVersion = "AG9.1";
        }

        if (multipleUsersPerSession == null || multipleUsersPerSession.contains("undefin") || multipleUsersPerSession.contains("null")) {
            multipleUsersPerSession = "false";
        }


        Integer numberOfSessions = 1;
        try {
            numberOfSessions = Integer.valueOf(numberOfSession.trim());
        } catch (Exception e) {
            System.out.println("Error numberOfSessions NOT a number");
        }
        numberOfSessions = numberOfSessions > 10 ? 10 : numberOfSessions;
        String accountKey = getAccountByName(epmIP, dbUser, dbPassword, accountName, "ACCOUNT_KEY");

        // System.out.println("accountKey=" + accountKey );
        Integer epId = -1;
        String messageOut = "";
        String epmURL = EPIP.split(":")[0];
        Integer epmURLPort = Integer.valueOf(EPIP.split(":")[1]);
        if (url != null & url.length() > 1) {
            epmURL = url.split(":")[0];
            epmURLPort = Integer.valueOf(url.split(":")[1]);
        }
        boolean isSSL = epmURLPort.compareTo(443) == 0;
        Configuration.epmSsl = isSSL;
        HttpCommManager.changeToHttpAgentURL();
        HttpCommManager.prepareBaseUrl(epmURL, epmURLPort);
        System.out.println("======================================");
        System.out.println("generateEP url=>" + epmURL + " and port " + epmURLPort + " is SSL=" + isSSL);
        System.out.println("======================================");

        if (device.toLowerCase().startsWith("smartphone")) {
            Configuration.accountKey = accountKey;
            String device_id = RandUtil.randomString(4) + "-" + RandUtil.randomString(4);
            String capability_ver = device.toLowerCase().contains("ios") ? "IOS-SDK-1.0" : "ANDROID-SDK-1.0";
            String companyName = device.toLowerCase().contains("ios") ? "Apple" : "Samsung";
            String mobileAttr = "[{ \"APPLICATION_NAME\" : \"Mobile Generic Attributes\", \"APP_EVENT_TIMESTAMP\" : 1465288234139, \"APP_EVENT_NAME\" : \"Mobile Generic Attributes\", \"APP_CONTEXT_MAP\" : { \"0.5\" : \""
                    + hostname + "\", \"0.8\" : \"" + companyName + "\" , \"0.9\" : \"" + hostname
                    + " smartphone\", \"8.2\" : \"Smartphone\", \"0.0\" : \"" + device_id + "\"} }]";
            HashMap<String, String> mobileDeviceAttributes  = new HashMap<>();
            try {
                mobileDeviceAttributes = MobileDeviceAttributes.getMobileAttributes(epmIP, dbUser, dbPassword);
            } catch (Exception e) {}

            String message = MobileBuilder.sendMobileAttr(mobileDeviceAttributes, epmURL, capability_ver, device_id, mobileAttr, isSSL);
            message = message.contains("Got Exception") ? message : "";
            return ("Connect Smartphone " + capability_ver + " with  device_id=" + device_id + "  =>" + message);
        }
        if (device.toLowerCase().startsWith("mac")) {
            HttpCommManager.changeToMacURL();
            Configuration.accountKey = null;
            String device_id = RandUtil.randomString(4) + "-" + RandUtil.randomString(4);
            HashMap<Integer, List<String>> staticAttr = new HashMap<Integer, List<String>>();
            staticAttr.put(32017,  Arrays.asList(device_id));
            staticAttr.put(14,  Arrays.asList(hostname));
            staticAttr.put(11,  Arrays.asList("190.1.0.0/2"));
            staticAttr.put(-1,  Arrays.asList("0"));
            staticAttr.put(92,   Arrays.asList("false"));
            staticAttr.put(-11,  Arrays.asList("1"));
            Integer agentIdFromEpm = null;
            try {
                agentIdFromEpm =  HttpComm.sendHttpMacConnMessage(1, epmURL, epmURLPort, agentVersion, hostname, staticAttr, accountKey);
            } catch (Exception e) {
                return( "Got Exception " + e.getMessage());
            }
            HttpCommManager.changeToHttpAgentURL();
            return("Connect MAC with  device_id " + device_id + " and epId =" + agentIdFromEpm);
        }
        try {
            String useDevice = device.contains("session") ? "Server" : device;
            if (epmURLPort.compareTo(443) == 0) {
                SslHelper.trustSelfSignedSSL();
                epId = HttpCommManager.sendHttpConnMessage(true, epmURL, epmURLPort, agentVersion, hostname, useDevice,
                        vdi, false, accountKey, "", "Trusted", deviceModelString);
            } else {
                epId = HttpCommManager.sendHttpConnMessage(false, epmURL, epmURLPort, agentVersion, hostname, useDevice, vdi,
                        false, accountKey, "", "Trusted", deviceModelString);
            }
            System.out.println("sendHttpConnMessage" + epmURL + epmURLPort + hostname);
            //System.out.println("epId=" + epId);
            //System.out.println("useDevice=" + useDevice);
        } catch (Exception e) {
            return("Got Exception: " + e.getMessage());
        }
        if (device.contains("RDP session") || device.contains("ICA session")) {
            try {
                messageOut += "--PHYSICAL EP=" + epId + "---";
                messageOut += "Sessions connected (session ID in brackets):";
                String sessionType = device.contains("RDP session")
                        ? OutgoingSessionUpdateHttpMessage.SessionType.RDP.toString()
                        : OutgoingSessionUpdateHttpMessage.SessionType.ICA.toString();
                String ipPrefix = device.contains("RDP session") ? "1.1.1." : "1.1.2.";
                for (int i = 1; i <= numberOfSessions; i++) {
                    String username =  "User-" + hostname;
                    if (multipleUsersPerSession.equalsIgnoreCase("true")){
                        username += "-" + i;
                    }
                    IncomingPathIdMessage incomingPathIdMessage = HttpCommManager.sendHttpSessionConnMessage(epmURL,
                            epmURLPort, epId.intValue(), username, sessionType + "-" + hostname + "-" + i,
                            ipPrefix + i, sessionType, accountKey);

                    System.out.println(
                            accountKey + "=>" + incomingPathIdMessage.pathId + "=>" + incomingPathIdMessage.sessionId);
                    if (incomingPathIdMessage.sessionId > 0
                            || (incomingPathIdMessage.pathId > 0 && accountKey.equalsIgnoreCase("-1"))) {
                        Integer sessionID = accountKey != null && accountKey.equalsIgnoreCase("-1")
                                ? incomingPathIdMessage.pathId : incomingPathIdMessage.sessionId;
                        messageOut += "pathId=" + incomingPathIdMessage.pathId + ", Session-" + hostname + "-" + i
                                + " (" + sessionID + "),";
                    } else {
                        messageOut += "Fail to connect session !!! *** Update 'License Provisioning' on your server settings *** ";
                    }
                }
                return(messageOut);
            } catch (Exception e) {
                return("Got Exception: " + e.getMessage());
            }
        }
        if (epId == null) {
            messageOut += "Fail to Generate EP got epId=null (HTTP status not 200 ???)";
        } else if (epId == -1) {
            messageOut += "Generate EP OK for hostname :" + hostname + " For accountKey :" + accountKey
                    + ", epId = FAIL";
        } else {
            messageOut += "Generate EP OK for hostname :" + hostname + " For accountKey :" + accountKey + ", epId ="
                    + epId;
        }
        return(messageOut);

    }

    @RequestMapping("/ArcTupleBuilder")
    public static String executeQuery(@RequestParam(name="query", required=false) String query,
                                    @RequestParam(name="application", required=false) String application,
                                    @RequestParam(name="activity", required=false) String activity,
                                    @RequestParam(name="appType", required=false) String appType,
                                    @RequestParam(name="epmIP", required=false) String epmIP,
                                    @RequestParam(name="dbUser", required=false) String dbUser,
                                    @RequestParam(name="accountName", required=false) String accountName,
                                    @RequestParam(name="dbPassword", required=false) String dbPassword)  throws Exception {

        String accountID = getAccountByName(epmIP, dbUser, dbPassword, accountName, "ACCOUNT_ID");

        if (query.equalsIgnoreCase("application")) {
            return(getApplications(epmIP, dbUser, dbPassword, appType, accountID));
        }
        if (query.equalsIgnoreCase("activity")) {
            return(getActivities(epmIP, dbUser, dbPassword, appType, application, accountID));
        }
        if (query.equalsIgnoreCase("contextuals")) {
            return(getContextuals(epmIP, dbUser, dbPassword, appType,application, activity));
        }
        if (query.equalsIgnoreCase("measurments")) {
            return(getMeasurments(epmIP, dbUser, dbPassword, appType, application, activity));
        }
        if (query.equalsIgnoreCase("MobileActivity")) {
            return(MobileMonitors.getActivities(epmIP, dbUser, dbPassword, application));
        }
        return("NA");
    }

    public static String getContextuals(String epmIP, String userDB, String passwordDB, String appType, String application,
                                        String activity) throws Exception {
        String query = "select distinct NAME  from application_view join transaction_contextual on application_view.ACTIVITY_ID=transaction_contextual.transaction_id join monitor mon on mon.id=application_view.application_id"
                + " where  APPLICATION_NAME='" + application + "' and ACTIVITY_NAME='" + activity + "'";
        if (appType != null){
            query = "select distinct act.pretty_name,mon.monitor_type  from application_view app join activity_ctx_attr_vw act on app.ACTIVITY_ID=act.activity_id join monitor mon on mon.id=app.application_id"
                    + " where  APPLICATION_NAME='" + application + "' and ACTIVITY_NAME='" + activity + "'  and monitor_type='"  +appType + "'";
        }
        return getQueryResponse(epmIP, userDB, passwordDB, query, "pretty_name");
    }

    public static String getMeasurments(String epmIP, String userDB, String passwordDB, String appType,String application,
                                        String activity) throws Exception {
        String query = "select distinct ma_name from ma_vw where monitor_type ='" + appType + "' and monitor_name='" + application + "' and activity_name='" + activity + "' and ma_name !='Availability' and ma_name NOT LIKE '%deleted on%'"; //move to new ma_vw
        return getQueryResponse(epmIP, userDB, passwordDB, query, "MA_NAME");
    }
    public static String getActivities(String epmIP, String userDB, String passwordDB, String monitorType,
                                       String application, String accountID) throws Exception {
        String query = "select distinct activity_name from ma_vw where monitor_name = '" + application
                + "'  and monitor_type = '" +  monitorType + "' and account_id=" + accountID;
        return getQueryResponse(epmIP, userDB, passwordDB, query, "ACTIVITY_NAME");
    }
    public static String getApplications(String epmIP, String userDB, String passwordDB, String monitorType,
                                         String accountID) throws Exception {

        String query = !accountID.equalsIgnoreCase("-1")
                ? "select PRETTY_NAME from   monitor where monitor_type='" + monitorType + "' and account_id='"
                + accountID + "' and PRETTY_NAME NOT LIKE '%deleted on%'"
                : "select PRETTY_NAME from   monitor where monitor_type='" + monitorType + "'  and PRETTY_NAME NOT LIKE '%deleted on%'";
        return getQueryResponse(epmIP, userDB, passwordDB, query, "PRETTY_NAME");
    }
    public static String getQueryResponse(String epmIP, String userDB, String passwordDB, String query,
                                          String resultString) throws Exception {
        String response = "[";
        Statement statement = DBUtil.getStatement(epmIP, userDB, passwordDB);
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            response = response + "\"" + result.getString(resultString) + "\",";
        }
        if (response.length() == 1){
            return "[]";
        }
        return response.substring(0, response.length() - 1) + "]";
    }


}

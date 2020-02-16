package com.aternity.tupleBuilder.controller;

import com.aternity.agentSimulator.simulator.commManager.http.HttpCommManager;
import com.aternity.tupleBuilder.ArcTuple.BasicArcMessage;
import com.aternity.tupleBuilder.ArcTuple.HttpComm;
import com.aternity.tupleBuilder.ArcTuple.OutgoingJsonMessage;
import com.aternity.tupleBuilder.mobile.MobileBuilder;
import com.aternity.tupleBuilder.mobile.MobileDeviceAttributes;
import com.aternity.tupleBuilder.mobile.MobileDeviceResources;
import com.aternity.tupleBuilder.utils.RandUtil;
import com.aternity.tupleBuilder.utils.SslHelper;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.aternity.tupleBuilder.controller.ArcTupleBuilder.getAccountByName;
import static com.aternity.tupleBuilder.controller.ArcTupleBuilder.getStatics;
import static com.aternity.tupleBuilder.controller.ArcTupleBuilder.getTransformedTuple;

@RestController
public class ConnectionController {

    static {
        HttpCommManager.initLog("");
        HttpCommManager.simulatorOnSingleRequestMode = true;
    }

    @RequestMapping(value = "/getTupleMessage", method = RequestMethod.POST)
    public String getTupleMessage(@RequestParam(name="postData[epmIP]", required=false) String epmIP,
                                  @RequestParam(name="postData[dbUser]", required=false) String dbUser,
                                  @RequestParam(name="postData[dbPassword]", required=false) String dbPassword,
                                  @RequestParam(name="postData[accountName]", required=false) String accountName,
                                  @RequestParam(name="postData[data]", required=false) String data) throws Exception {
        return getTransformedTuple(epmIP, dbUser, dbPassword, data, "EP_ID", "EP_ID", accountName, false, false);
    }

    @RequestMapping(value = "/getMobileAttributesOptionsByDevice", method = RequestMethod.GET)
    public String getMobileAttributesOptionsByDevice(@RequestParam(name="device", required=false) String device) {
        return (MobileDeviceResources.getMobileAttributesByDevice(device).toString().replaceAll("\\{", "")
                .replaceAll("\\}", ""));
    }

    @RequestMapping(value = "/executeTupleWithReadyData", method = RequestMethod.POST)
    public String executeTupleWithReadyData(@RequestParam(name="postData[epmIP]", required=false) String epmIP,
                             @RequestParam(name="postData[userDB]", required=false) String userDB,
                             @RequestParam(name="postData[accountName]", required=false) String account,
                             @RequestParam(name="postData[passwordDB]", required=false) String passwordDB,
                             @RequestParam(name="postData[virtualEpId]", required=false) String virtualEpId,
                             @RequestParam(name="postData[globalEpId]", required=false) String globalEpId,
                             @RequestParam(name="postData[url]", required=false) String EPMurl,
                             @RequestParam(name="postData[data]", required=false) String data,
                             @RequestParam(name="postData[isMac]", required=false) String isMac) throws Exception {
        String urlToSend = epmIP.split(":")[0];
        Integer EPMPort = 80;
        if (EPMurl != null & EPMurl.length() > 1) {
            urlToSend = EPMurl.split(":")[0];
            EPMPort = Integer.valueOf(EPMurl.split(":")[1]);
            System.out.println("======================================");
            System.out.println("executeTuple url=>" + urlToSend + " and port " + EPMPort);
            System.out.println("======================================");
        }
        String url = "http://" + urlToSend + ":" + EPMPort + "/Httpagent/Tuple?epId=" + globalEpId
                + "&configId=12&enabledMonitorsConfigId=0";
        if (EPMPort.compareTo(443) == 0) {
            SslHelper.trustSelfSignedSSL();
            url = url.replaceAll("http://", "https://");
        }
        if (isMac.contains("true")){
            url = "http://" + urlToSend +"/MacAgent/Tuple?epId="     + globalEpId + "&configId=12&enabledMonitorsConfigId=0";
            String accountKey = getAccountByName(epmIP, userDB, passwordDB, account, "ACCOUNT_KEY");
            if (accountKey != null) {
                url += "&account=" + accountKey;
            }
            HttpComm httpComm = new HttpComm(url);
            httpComm.sendRequest(new HashMap<String, String>() ,  data.getBytes());
            return("Send Data to " + url + "  " + data + " =====>>>>> Response OK !!!");
        }
        File jsonDataFile = new File("C:\\temp\\jsonDataFile.json");
        FileUtils.writeStringToFile(jsonDataFile, data, "UTF-8");
        HttpComm httpComm = new HttpComm(url);
        HashMap<String, String> params = new HashMap<String, String>();
        BasicArcMessage msg = new BasicArcMessage(BasicArcMessage.HttpMessages.tuple.getMessageId(), true, jsonDataFile.getAbsolutePath());
        byte[] msgData = msg.encode();
        httpComm.sendRequest(params, msgData);
        Thread.sleep(200);
        FileUtils.deleteQuietly(jsonDataFile);
        if (data.startsWith("Got exception")) {
            return ("FAIL to send Data to " + url + "  " + data);
        } else {
            return("Send Data to " + url + "  " + data + " =====>>>>> Response OK !!!");
        }
    }

    @RequestMapping(value = "/sendMobileActivity", method = RequestMethod.GET)
    public String sendMobileActivity(@RequestParam(name="epmIP", required=false) String epmIP,
                                          @RequestParam(name="userDB", required=false) String userDB,
                                          @RequestParam(name="passwordDB", required=false) String passwordDB,
                                          @RequestParam(name="mobileAttr", required=false) String mobileAttr,
                                          @RequestParam(name="packageName", required=false) String packageName,
                                          @RequestParam(name="url", required=false) String epmURL,
                                          @RequestParam(name="activity", required=false) String activity,
                                          @RequestParam(name="oid", required=false) String device_id,
                                          @RequestParam(name="capability_ver", required=false) String capability_ver,
                                          @RequestParam(name="sdkEvent", required=false) String sdkEvent) throws Exception {

        boolean isSSL = epmURL.contains(":443");
        epmURL = epmIP.split(":")[0];
        HashMap<String, String> mobileDeviceAttributes  = new HashMap<>();
        try {
            mobileDeviceAttributes = MobileDeviceAttributes.getMobileAttributes(epmIP, userDB, passwordDB);
        } catch (Exception e) {}

        String message = MobileBuilder.sendMobileActivityEvent(mobileDeviceAttributes, epmURL, capability_ver, device_id, mobileAttr,
                packageName, activity, sdkEvent, isSSL);
        if (!message.contains("Got Exception")) {
            if (sdkEvent.contains("Start") && sdkEvent.contains("End")) {
                System.out.println("Mobile Start & End => wait 1 second and send End event !!!!");
                Thread.sleep(1000);
                MobileBuilder.sendMobileActivityEvent(mobileDeviceAttributes, epmURL, capability_ver, device_id, mobileAttr, packageName,
                        activity, "End", isSSL);
            }
            return("Send mobile Activity Event " + activity + " to device_id " + device_id + " OK !!!");
        } else {
            return("FAIL to Send mobile Activity Event to device_id " + device_id + " !!!");
        }
    }

    @RequestMapping(value = "/sendMobileAttr", method = RequestMethod.GET)
    public  String sendMobileAttr(@RequestParam(name="epmIP", required=false) String epmIP,
                                      @RequestParam(name="userDB", required=false) String userDB,
                                      @RequestParam(name="passwordDB", required=false) String passwordDB,
                                      @RequestParam(name="mobileAttr", required=false) String mobileAttr,
                                      @RequestParam(name="url", required=false) String epmURL,
                                      @RequestParam(name="oid", required=false) String device_id,
                                      @RequestParam(name="allMobileDeviceAttr", required=false) String allMobileDeviceAttr,
                                      @RequestParam(name="capability_ver", required=false) String capability_ver) throws Exception {

        boolean isSSL = epmURL.contains(":443");
        epmURL = epmIP.split(":")[0];
        HashMap<String, String> mobileDeviceAttributes = new HashMap<>();;
        try {
            mobileDeviceAttributes = MobileDeviceAttributes.getMobileAttributes(epmIP, userDB, passwordDB);
        } catch (Exception e) {
            return("sendMobileAttr: failed");
        }

        if (allMobileDeviceAttr.length() == 0) { // No Device Resources data
            return("Send mobile attributes " + mobileAttr + " to device_id " + device_id + " ==>"
                    + MobileBuilder.sendMobileAttr(mobileDeviceAttributes, epmURL, capability_ver, device_id, mobileAttr, isSSL));
        } else { // send with AppEvent
            return("Send mobile event to device_id " + device_id + "  => " + MobileBuilder
                    .sendMobileAppEvent(mobileDeviceAttributes, epmURL, capability_ver, device_id, mobileAttr, allMobileDeviceAttr, isSSL));
        }

    }

    @RequestMapping(value = "/sendStatic", method = RequestMethod.POST)
    public String sendStatic(@RequestParam(name="postData[epmIP]", required=false) String epmIP,
                                  @RequestParam(name="postData[userDB]", required=false) String userDB,
                                  @RequestParam(name="postData[IsSession]", required=false) String isSession,
                                  @RequestParam(name="postData[sessionID]", required=false) String sessionID,
                                  @RequestParam(name="postData[accountName]", required=false) String account,
                                  @RequestParam(name="postData[passwordDB]", required=false) String passwordDB,
                                  @RequestParam(name="postData[static]", required=false) String staticValue,
                                  @RequestParam(name="postData[globalEpId]", required=false) String globalEpId,
                                  @RequestParam(name="postData[url]", required=false) String EPMurl,
                                  @RequestParam(name="postData[isMac]", required=false) String isMac) throws Exception {


        HashMap<String, String> staticsHash = getStatics(epmIP, userDB, passwordDB);
        String urlToSend = epmIP.split(":")[0];
        Integer EPMPort = 80;

        if (EPMurl != null && EPMurl.length() > 1) {
            urlToSend = EPMurl.split(":")[0];
            EPMPort = Integer.valueOf(EPMurl.split(":")[1]);
            System.out.println("======================================");
            System.out.println("sendStatic:Using url=>" + urlToSend + " and port " + EPMPort);
            System.out.println("======================================");
        }

        String url = "http://" + urlToSend + ":" + EPMPort + "/Httpagent/Report?epId=" + globalEpId
                + "&configId=-1&enabledMonitorsConfigId=0";
        if (EPMPort.compareTo(443) == 0) {
            SslHelper.trustSelfSignedSSL();
            url = url.replaceAll("http://", "https://");
        }
        String accountKey = getAccountByName(epmIP, userDB, passwordDB, account, "ACCOUNT_KEY");
        if (accountKey != null) {
            url += "&account=" + accountKey;
        }
        String sessionStatistics = "";

        if (isSession.equalsIgnoreCase("1") || isMac.equalsIgnoreCase("true")) {
            if (isMac.equalsIgnoreCase("true")){
                sessionStatistics = "[\n{\"epId\": " + globalEpId + ", \"singleAttrValues\": { \n";
            } else {
                sessionStatistics = "[\n{\"epId\": " + sessionID + ", \"singleAttrValues\": { \n";
            }
            for (String staticVal : staticValue.split(",")) {
                String value = staticVal.split("=")[1];
                value = value.equalsIgnoreCase("[VALUE]") ? RandUtil.randomString(RandUtil.randInt(3, 12)) : value;
                sessionStatistics += "\"" + staticsHash.get(staticVal.split("=")[0]) + "\" :\"" + value + "\",\n";
            }
            sessionStatistics = sessionStatistics.endsWith(",\n")
                    ? sessionStatistics.substring(0, sessionStatistics.length() - 2) : sessionStatistics;
            sessionStatistics += "}}]";
            //	sessionStatistics = sessionStatistics.replaceAll("@@@", ",");
        }
        String staticWithId = "";
        for (String staticVal : staticValue.split(",")) {
            String value = staticVal.split("=")[1];
            value = value.equalsIgnoreCase("[VALUE]") ? RandUtil.randomString(RandUtil.randInt(3, 12)) : value;
            staticWithId += staticsHash.get(staticVal.split("=")[0]) + "=" + value + ",";
        }
        staticWithId = staticWithId.endsWith(",") ? staticWithId.substring(0, staticWithId.length() - 1) : staticWithId;

        //this replacement for escaping issues to enable pass comma as static value
        //The syndicator server automatically replaces &&& with ,
        sessionStatistics = sessionStatistics.replaceAll("@@@", "&&&");
        staticWithId= staticWithId.replaceAll("@@@", "&&&");
        if (isSession.equalsIgnoreCase("0") && isMac.equalsIgnoreCase("false")) {
            try {
                if (accountKey != null) {
                    HttpCommManager.sendHttpReportMessage(urlToSend, EPMPort, Integer.valueOf(globalEpId), staticWithId,
                            accountKey);
                } else {
                    HttpCommManager.sendHttpReportMessage(urlToSend, EPMPort, Integer.valueOf(globalEpId),
                            staticWithId);
                }
            } catch (Exception e) {
                System.err.println("sendHttpReportMessage " + e.getMessage());
            }
            return(url + " Data=> " + staticWithId);
        } else { // singleAttrValues json structure on Session or MAC
            try {
                if (isMac.equalsIgnoreCase("true")){
                    url = "http://" + urlToSend +"/MacAgent/Report?epId="     + globalEpId + "&configId=12";
                    if (accountKey != null) {
                        url += "&account=" + accountKey;
                    }
                    Map<String, Object> params = HttpCommManager.createParams(Integer.valueOf(globalEpId), -1, 0);
                    HttpComm httpComm = new HttpComm(url);
                    httpComm.sendRequest(new HashMap<String, String>() ,  sessionStatistics.getBytes());
                    //HttpCommManager.prepareHttpCom(urlToSend, EPMPort ,HttpMessageType.Report).sendRequest(params ,  sessionStatistics.getBytes());

                    return("Send Data to " + url + "  " + sessionStatistics + " =====>>>>> Response OK !!!");
                } else {
                    OutgoingJsonMessage msg = new OutgoingJsonMessage('t', true, sessionStatistics);
                    byte[] msgData = msg.encode(sessionStatistics.replaceAll("\n", ""));
                    HttpComm httpComm = new HttpComm(url);
                    HashMap<String, String> params = new HashMap<String, String>();
                    httpComm.sendRequest(params, msgData);
                }
            } catch (Exception e) {
                System.err.println("sendJsonMsgForHttpAgent exception" + e.getMessage());
            }
            return(url + " Data=> " + sessionStatistics);
        }

    }

    @RequestMapping(value = "/executeMessageWithData", method = RequestMethod.POST)
    public static String executeMessageWithData(@RequestParam(name="postData[data]", required=false) String data,
                                              @RequestParam(name="postData[url]", required=false) String url,
                                              @RequestParam(name="postData[opcode]", required=false) String opcode,
                                              @RequestParam(name="postData[isZip]", required=false) String isZipString)throws Exception {

        boolean isZip = Boolean.valueOf(isZipString);
        HttpComm httpComm =  new HttpComm(url);
        BasicArcMessage msg = new BasicArcMessage(opcode.charAt(0), true, null, data);
        byte[] msgData = msg.encode();;
        String response = httpComm.invokeGenericJsonCall(url, new HashMap<String, String>(), msgData, isZip);

        return(response);
    }

    @RequestMapping(value = "/executeTuple", method = RequestMethod.POST)
    public static String executeTuple(@RequestParam(name="postData[epmIP]", required=false) String epmIP,
                                      @RequestParam(name="postData[data]", required=false) String data,
                                      @RequestParam(name="postData[dbUser]", required=false) String dbUser,
                                      @RequestParam(name="postData[accountName]", required=false) String accountName,
                                      @RequestParam(name="postData[globalEpId]", required=false) String globalEpId,
                                      @RequestParam(name="postData[dbPassword]", required=false) String dbPassword,
                                      @RequestParam(name="postData[url]", required=false) String EPMurl,
                                      @RequestParam(name="postData[virtualEpId]", required=false) String virtualEpId,
                                      @RequestParam(name="postData[appInternalSection]", required=false) String appInternalSectionAsString,
                                      @RequestParam(name="postData[isWifi]", required=false) String isWifiAsString,
                                      @RequestParam(name="postData[isMac]", required=false) String isMac,
                                      @RequestParam(name="postData[testConnection]", required=false) String testConnection) throws Exception {

        virtualEpId = virtualEpId == null || virtualEpId != null && virtualEpId.length() == 0 ? globalEpId : virtualEpId;

        boolean appInternalSection = Boolean.valueOf(appInternalSectionAsString);
        boolean wifiSection = Boolean.valueOf(isWifiAsString);

        if (data.contains("Application Discovery Application Events")) {

            data = data.replace("JsonData=[VALUE]",
                    "JsonData={\\\"TimeElapsed\\\":\\\"" + RandUtil.randInt(4000, 5000) + "\\\"}");
        }
        if (data.contains("Application Usage - Desktop")) {

            data = data.replace("JsonData=[VALUE]",
                    "JsonData={\\\"ActiveTime\\\":" + RandUtil.randInt(8000, 15000) + "&&&\\\"WaitTime\\\":" + RandUtil.randInt(80, 150)
                            + "&&&\\\"CursorWaitTime\\\":" + RandUtil.randInt(0, 5) + "&&&\\\"ForegroundTime\\\":"
                            + RandUtil.randInt(100000, 120000) + "&&&\\\"HungTime\\\":" + RandUtil.randInt(0, 5) + "}");
        }
        if (data.contains("Application Usage - Web")) {
            data = data.replace("Host Name=[VALUE]",
                    "Host Name={\\\"ActiveTime\\\":" + RandUtil.randInt(8000, 15000) + "&&&\\\"WaitTime\\\":" + RandUtil.randInt(80, 150)
                            + "&&&\\\"CursorWaitTime\\\":" + RandUtil.randInt(0, 5) + "&&&\\\"ForegroundTime\\\":"
                            + RandUtil.randInt(40000, 50000) + "&&&\\\"HungTime\\\":" + RandUtil.randInt(0, 5) + "&&&\\\"LoadTime\\\":"
                            + RandUtil.randInt(100, 200) + "&&&\\\"Host\\\":\\\"https://www.aternity.com" + RandUtil.randInt(0, 10)
                            + "\\\"}");
        }
        data = getTransformedTuple(epmIP, dbUser, dbPassword, data, globalEpId, virtualEpId, accountName, appInternalSection, wifiSection);
        // System.out.println(data);
        File jsonDataFile = new File("C:\\temp\\jsonDataFile.json");
        FileUtils.writeStringToFile(jsonDataFile, data, "UTF-8");

        String urlToSend = epmIP.split(":")[0];
        Integer EPMPort = 80;
        if (EPMurl != null & EPMurl.length() > 1) {
            urlToSend = EPMurl.split(":")[0];
            EPMPort = Integer.valueOf(EPMurl.split(":")[1]);
            System.out.println("======================================");
            System.out.println("executeTuple url=>" + urlToSend + " and port " + EPMPort);
            System.out.println("======================================");
        }
        String url = "http://" + urlToSend + ":" + EPMPort + "/Httpagent/Tuple?epId=" + globalEpId
                + "&configId=12&enabledMonitorsConfigId=0";
        if (EPMPort.compareTo(443) == 0) {
            SslHelper.trustSelfSignedSSL();
            url = url.replaceAll("http://", "https://");
        }
        if (isMac.contains("true")){
            url = "http://" + urlToSend +"/MacAgent/Tuple?epId="     + globalEpId + "&configId=12&enabledMonitorsConfigId=0";
            Map<String, Object> params = HttpCommManager.createParams(Integer.valueOf(globalEpId), -1, 0);
            String accountKey = getAccountByName(epmIP, dbUser, dbPassword, accountName, "ACCOUNT_KEY");
            if (accountKey != null) {
                url += "&account=" + accountKey;
            }
            HttpComm httpComm = new HttpComm(url);
            httpComm.sendRequest(new HashMap<String, String>() ,  FileUtils.readFileToByteArray(jsonDataFile));

            return("Send Data to " + url + "  " + data + " =====>>>>> Response OK !!!");
        }
        HttpComm httpComm = new HttpComm(url);
        HashMap<String, String> params = new HashMap<String, String>();
        BasicArcMessage msg = new BasicArcMessage(BasicArcMessage.HttpMessages.tuple.getMessageId(), true,
                jsonDataFile.getAbsolutePath());
        byte[] msgData = msg.encode();
        httpComm.sendRequest(params, msgData);
        Thread.sleep(200);
        FileUtils.deleteQuietly(jsonDataFile);
        if (data.startsWith("Got exception")) {
            return("FAIL to send Data to " + url + "  " + data);
        } else {
            return("Send Data to " + url + "  " + data + " =====>>>>> Response OK !!!");
        }
    }
}

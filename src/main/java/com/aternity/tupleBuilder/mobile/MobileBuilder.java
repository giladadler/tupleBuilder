package com.aternity.tupleBuilder.mobile;

import java.util.HashMap;


public class MobileBuilder {

	static String LAST_REBOOT_TIME_ATTR_NAME = "Last Reboot Time";

	public static String sendMobileAttr(HashMap<String, String> mobileDeviceAttributes, String epmURL, String capability_ver, String device_id, String mobileAttr, boolean isSSL)
			throws Exception {

		capability_ver = capability_ver == null ? "IOS-SDK-1.0" : capability_ver;
		String mobileURL = getURL(epmURL, isSSL);
		System.out.println("mobileURL= " + mobileURL);
		try {
			com.aternity.tupleBuilder.ArcTuple.HttpComm httpComm = new com.aternity.tupleBuilder.ArcTuple.HttpComm(mobileURL);
			httpComm.init(isSSL);
			HashMap<String, Object> params = populateParams ("com.aternity.agent", "HandshakeAttributes" , device_id, capability_ver);

			String MobileGenericAttributes = "{";
			String attributes = "";
			if (mobileAttr.startsWith("[{")) { // already prepared as json
				// Do nothing
				params.put("attributes", mobileAttr);
				attributes = mobileAttr;
			} else {
				mobileAttr = mobileAttr.endsWith(",") ? mobileAttr : mobileAttr + ",";
				//System.out.println("device_id= " + device_id);
				//System.out.println("mobileURL= " + mobileURL);
				for (String attr : mobileAttr.split(",")) {
					String attrName = attr.split("=")[0];
					String attrValue = attr.split("=")[1];
					String attrValueForSend = "\":\"" + attrValue + "\",";

					if(attrName.equalsIgnoreCase(LAST_REBOOT_TIME_ATTR_NAME)){ // send as number, not as test string - without quotes
						attrValueForSend = "\":" + attrValue + ",";
					}
					MobileGenericAttributes += "\""
							+ mobileDeviceAttributes.get(attrName) + attrValueForSend;
				}
				MobileGenericAttributes = MobileGenericAttributes.endsWith(",")
						? MobileGenericAttributes.substring(0, MobileGenericAttributes.length() - 1)
						: MobileGenericAttributes;
				MobileGenericAttributes += "}";
				attributes = "[{ \"APPLICATION_NAME\" : \"Mobile Generic Attributes\", \"APP_EVENT_TIMESTAMP\" : "
						+ String.valueOf(System.currentTimeMillis())
						+ ", \"APP_EVENT_NAME\" : \"Mobile Generic Attributes\", \"APP_CONTEXT_MAP\" : "
						+ MobileGenericAttributes + " }]";
				params.put("attributes", attributes);
			}
			httpComm.invokeCall(mobileURL, params, httpComm.getHttpClient());
			return attributes;
		} catch (Exception e) {
			return "Got Exception " + e.getMessage();
		}
	}

	public static String sendMobileAppEvent(HashMap<String, String> mobileDeviceAttributes, String epmURL, String capability_ver, String device_id, String mobileAttr,
			String deviceResourceAttr, boolean isSSL) throws Exception {

		capability_ver = capability_ver == null ? "IOS-SDK-1.0" : capability_ver;
		String mobileURL = getURL(epmURL, isSSL);
		deviceResourceAttr = deviceResourceAttr.replaceAll("TIMESTAMP_PLACE_HOLDER", String.valueOf(System.currentTimeMillis()));
		try {
			com.aternity.tupleBuilder.ArcTuple.HttpComm httpComm = new com.aternity.tupleBuilder.ArcTuple.HttpComm(mobileURL);
			HashMap<String, Object> params = populateParams ("com.aternity.agent", "AppEvents" , device_id, capability_ver);

			String MobileGenericAttributes = "{";

			mobileAttr = mobileAttr.endsWith(",") ? mobileAttr : mobileAttr + ",";

			for (String attr : mobileAttr.split(",")) {
				MobileGenericAttributes += "\"" + mobileDeviceAttributes.get(attr.split("=")[0])
						+ "\":\"" + attr.split("=")[1] + "\",";
			}
			MobileGenericAttributes = MobileGenericAttributes.endsWith(",")
					? MobileGenericAttributes.substring(0, MobileGenericAttributes.length() - 1)
					: MobileGenericAttributes;
			MobileGenericAttributes += "}";
			String attributes = "[{ \"APPLICATION_NAME\" : \"Mobile Generic Attributes\", \"APP_CONTEXT_MAP\" : "
					+ MobileGenericAttributes + ",\"APP_EVENT_TIMESTAMP\" : "
					+ String.valueOf(System.currentTimeMillis())
					+ ", \"APP_EVENT_NAME\" : \"Mobile Generic Attributes\"}, ";
			attributes += getAllDeviceResourcesAsJson(deviceResourceAttr) + "]";

			//System.out.println("events=" + attributes);

			params.put("events", attributes);
			httpComm.invokeCall(mobileURL, params, httpComm.getHttpClient());
			return attributes;
		} catch (Exception e) {
			return "Got Exception " + e.getMessage();
		}
	}
	public static String sendMobileActivityEvent(HashMap<String, String> mobileDeviceAttributes, String epmURL, String capability_ver, String device_id, String mobileAttr,
			String packageName, String activity, String sdkEvent, boolean isSSL) throws Exception {

		sdkEvent = sdkEvent.contains("Start") && sdkEvent.contains("End") ? "Start" : sdkEvent;
		capability_ver = capability_ver == null ? "IOS-SDK-1.0" : capability_ver;
		String mobileURL = getURL(epmURL, isSSL);
		try {
			com.aternity.tupleBuilder.ArcTuple.HttpComm httpComm = new com.aternity.tupleBuilder.ArcTuple.HttpComm(mobileURL);
			httpComm.init(isSSL);
			HashMap<String, Object> params = populateParams (packageName, "AppEvents" , device_id, capability_ver);
			String MobileGenericAttributes = "{";

			mobileAttr = mobileAttr.endsWith(",") ? mobileAttr : mobileAttr + ",";
			for (String attr : mobileAttr.split(",")) {
				MobileGenericAttributes += "\"" + mobileDeviceAttributes.get(attr.split("=")[0])
						+ "\":\"" + attr.split("=")[1] + "\",";
			}
			MobileGenericAttributes = MobileGenericAttributes.endsWith(",")
					? MobileGenericAttributes.substring(0, MobileGenericAttributes.length() - 1)
					: MobileGenericAttributes;
			MobileGenericAttributes += "}";
			String attributes = "[{ \"APPLICATION_NAME\" : \"Mobile Generic Attributes\", \"APP_CONTEXT_MAP\" : "
					+ MobileGenericAttributes + ",\"APP_EVENT_TIMESTAMP\" : "
					+ String.valueOf(System.currentTimeMillis())
					+ ", \"APP_EVENT_NAME\" : \"Mobile Generic Attributes\"}, ";
			attributes += getActivityAsJson(packageName, activity, sdkEvent) + "]";

			//System.out.println("events=" + attributes);
			params.put("events", attributes);
			httpComm.invokeCall(mobileURL, params, httpComm.getHttpClient());
			return attributes;
		} catch (Exception e) {
			return("Got Exception " + e.getMessage());
		}
	}

	private static HashMap<String, Object> populateParams (String packageName, String msg_type, String device_id, String capability_ver){
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("pkg_name", packageName);
		params.put("msg_type", msg_type);
		params.put("device_id", device_id);
		params.put("capability_ver", capability_ver);
		params.put("cfg_ver", "0");
		return params;
	}
	
	private static String getURL(String epmURL, boolean isSSL) {
		String mobileURL = "http://" + epmURL + "/Mobile/DCGateway";
		if (isSSL){
			mobileURL = "https://" + epmURL + "/Mobile/DCGateway";
		}
		return mobileURL;
	}
	private static String getActivityAsJson(String packageName, String activity, String sdkEvent) {
		String json = "";	
		json += "{\"APPLICATION_NAME\" : \"" + packageName + "\",\"APP_CONTEXT_MAP\" : ";
		json += "{ \"SDK_EVENT_PHASE\" : \"" + sdkEvent + "\", \"9.4\" : \"" + packageName + "\", \"9.6\" : \"w1.7.3\", \"9.7\" : \"sdk-7.2.1\"}" ;
		json += ",\"APP_EVENT_TIMESTAMP\" : " + String.valueOf(System.currentTimeMillis())
				+ ",\"APP_EVENT_NAME\" : \"" + activity + "\"}";		
		//System.out.println("getActivityAsJson json = " +json);
		return json;
	}
	private static String getAllDeviceResourcesAsJson(String deviceResourceAttr) {
		String json = "";
		deviceResourceAttr = deviceResourceAttr.replaceAll("%20", " ");
		HashMap<String, String> deviceResources = new HashMap<String, String>();
		for (String resource : deviceResourceAttr.split(",")) {
			String currentValue = deviceResources.get(resource.split("_")[0]) != null
					? deviceResources.get(resource.split("_")[0]) + "," : "";
			deviceResources.put(resource.split("_")[0], currentValue + resource.split("_")[1]);
		}
		for (String resource : deviceResources.keySet()) {
			json += "{\"APPLICATION_NAME\" : \"" + resource + "\",\"APP_CONTEXT_MAP\" : {";
			String values = deviceResources.get(resource).endsWith(",") ? deviceResources.get(resource)
					: deviceResources.get(resource) + ",";
			for (String oid : values.split(",")) {
				json += "\"" + MobileDeviceResources.getOid(resource, oid.split("=")[0]) + "\":\"" + oid.split("=")[1]
						+ "\",";
			}
			json = json.substring(0, json.length() - 1);
			
			// ,\"9.5\" : \"WorxMail\", \"9.4\" : \"com.company.app1\"
			json += "},\"APP_EVENT_TIMESTAMP\" : " + String.valueOf(System.currentTimeMillis())
					+ ",\"APP_EVENT_NAME\" : \"" + resource + "\"},";
		}
		json = json.substring(0, json.length() - 1);
		
		//System.out.println("json = " +json);
		return json;
	}

}

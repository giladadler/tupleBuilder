package com.aternity.tupleBuilder.mobile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class MobileDeviceResources  {

	
	public static List<String> getDeviceResources (){
		List<String> MobileAttributes = new LinkedList<String>();
		MobileAttributes.add("Mobile Battery");
		MobileAttributes.add("Mobile Memory");
		MobileAttributes.add("Mobile Wi-Fi Network");
		MobileAttributes.add("Mobile Network");
		MobileAttributes.add("Mobile CPU");
		MobileAttributes.add("Mobile Storage");
		MobileAttributes.add("Top Processes");
		MobileAttributes.add("Mobile Error");
		MobileAttributes.add("Mobile Crash");
		MobileAttributes.add("Mobile App Data");
		MobileAttributes.add("HTTP");
		MobileAttributes.add("App Usage");
		MobileAttributes.add("App Usage Time");
		return MobileAttributes;
	}
	
	public static String getDefaultValueByDeviceResource (String device){
		HashMap<String, String> defaultValues = new HashMap<String, String>();
		defaultValues.put("Mobile App Data", "Mobile App Data_Network Outgoing Traffic=943.06,Mobile App Data_Network Incoming Traffic=12110.77,Mobile App Data_Application ID=aternity,Mobile App Data_Application Name=WorxMail,Mobile App Data_Application Version=w2.5.4-a,Mobile App Data_Network Type=Mobile,Mobile App Data_Server ID=192.168.2.120");
		defaultValues.put("Mobile Error", "Mobile Error_Application Name=WorxMail,Mobile Error_Application ID=aternity,Mobile Error_Application Version=w1.7.3,Mobile Error_Message Level=error,Mobile Error_Message Text=exception illegal value received and ignored");
		defaultValues.put("Mobile Crash", "Mobile Crash_Application Name=WorxMail,Mobile Crash_Application ID=aternity,Mobile Crash_Application Version=w2.1.0,Mobile Crash_Exception Code=[SampleAppController CheckFormView]:  inaccessible selector sent to instance 0x15c6159f0,Mobile Crash_Crash Thread=0\t_input_check\n1\thandle_form\n2\tuser_window\n3\t_kevent64\n4\t0x231149385\n,Mobile Crash_Crash Key=c011a,Mobile Crash_Crash Details Key=None,Mobile Crash_Memory Consumed=3630,Mobile Crash_Memory Utilization=45");
		defaultValues.put("HTTP", "HTTP_Network Outgoing Traffic=4179.80,HTTP_Network Incoming Traffic=31517.96,HTTP_Duration=2.56,HTTP_Status Code=200,HTTP_Application ID=aternity,HTTP_Application Name=WorxMail,HTTP_Application Version=w2.1.0-a,HTTP_Network Type=Mobile,HTTP_URL=https:\\/\\/m.samplecompany.com\\/calendar\\/items.html,HTTP_HTTP Method=GET,HTTP_Timestamp=TIMESTAMP_PLACE_HOLDER");
		defaultValues.put("App Usage Time", "App Usage Time_Time In Foreground=58,App Usage Time_Active Time=48,App Usage Time_Total Wait Time=1,App Usage Time_Application Name=MobileStore");
		defaultValues.put("App Usage", "App Usage_Application ID=aternity,App Usage_Application Name=MobileStore,App Usage_Application Version=s2.5.4,App Usage_SDK Version=sdk-7.2.3,App Usage_Event Type=Launch,App Usage_Activity Response=0");
		return defaultValues.get(device);
	}
	
	public static LinkedHashMap getMobileAttributesByDevice (String device){
		LinkedHashMap<String, String> MobileAttributes = new LinkedHashMap<String, String>();
		if (device.equalsIgnoreCase("Mobile Battery")){
			MobileAttributes.put("2.3", "Battery Level");	
			MobileAttributes.put("2.1", "Is Charging");
			MobileAttributes.put("2.4", "Health");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Memory")){
			MobileAttributes.put("6.3", "Is Low Memory");	
			MobileAttributes.put("6.2", "Memory Utilization");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Wi-Fi Network")){
			MobileAttributes.put("4.4", "Signal Strength");	
			MobileAttributes.put("4.3", "SSID");
			MobileAttributes.put("4.5", "BSSID");
			MobileAttributes.put("4.6", "Channel");
			MobileAttributes.put("4.7", "MAC address");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Network")){
			MobileAttributes.put("3.4", "Signal Strength");	
			MobileAttributes.put("1.4", "Mobile Carrier");
			MobileAttributes.put("3.5", "Network Type");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile CPU")){
			MobileAttributes.put("5.4", "CPU Utilization");	
			MobileAttributes.put("5.3", "Current CPU Clock Speed");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Storage")){
			MobileAttributes.put("7.2", "Free Megabytes");	
			MobileAttributes.put("7.1", "Percent Free Space");
			MobileAttributes.put("7.3", "Storage Name");	
			MobileAttributes.put("7.4", "Storage Type");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Top Processes")){
			MobileAttributes.put("10.2", "CPU Utilization");	
			MobileAttributes.put("10.3", "Memory Utilization");
			MobileAttributes.put("9.2", "Process Name (diverse)");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Error")){
			MobileAttributes.put("6.3", "Error Occurred");	
			MobileAttributes.put("9.4", "Application ID");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("9.6", "Application Version");
			MobileAttributes.put("11.2", "Message Tag");
			MobileAttributes.put("12.1", "Message Text");
			MobileAttributes.put("11.3", "Message Level");
			MobileAttributes.put("All-Mobile Error", "All-Mobile Error");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile Crash")){
			MobileAttributes.put("6.3", "Error Occurred");	
			MobileAttributes.put("9.4", "Application ID");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("9.6", "Application Version");
			MobileAttributes.put("11.5", "Exception Type");
			MobileAttributes.put("11.6", "Exception Code");
			MobileAttributes.put("11.7", "Crash Thread");
			MobileAttributes.put("11.8", "Crash Key");
			MobileAttributes.put("12.1", "Crash Details Key");
			MobileAttributes.put("14.1", "Memory Consumed");
			MobileAttributes.put("14.2", "Memory Utilization");
			MobileAttributes.put("All-Mobile Crash", "All-Mobile Crash");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("Mobile App Data")){
			MobileAttributes.put("Snd", "Network Outgoing Traffic");
			MobileAttributes.put("Rcv", "Network Incoming Traffic");
			MobileAttributes.put("9.4", "Application ID");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("9.6", "Application Version");
			MobileAttributes.put("11.4", "Network Type");
			MobileAttributes.put("12.3", "Server ID");
			MobileAttributes.put("All-Mobile App Data", "All-Mobile App Data");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("HTTP")){
			MobileAttributes.put("Snd", "Network Outgoing Traffic");
			MobileAttributes.put("Rcv", "Network Incoming Traffic");			
			MobileAttributes.put("13.1", "Response Time	");
			MobileAttributes.put("13.2", "Duration");
			MobileAttributes.put("13.3", "Status Code");
			MobileAttributes.put("11.1", "Transport Error");						
			MobileAttributes.put("9.4", "Application ID");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("9.6", "Application Version");
			MobileAttributes.put("11.4", "Network Type");
			MobileAttributes.put("12.3", "Server ID");			
			MobileAttributes.put("13.5", "URL");						
			MobileAttributes.put("12.2", "Truncated URL");
			MobileAttributes.put("13.4", "HTTP Method");
			MobileAttributes.put("13.6", "Timestamp");			
			MobileAttributes.put("All-HTTP", "All-HTTP");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("App Usage")){
			MobileAttributes.put("13.7", "Activity Response");	
			MobileAttributes.put("9.4", "Application ID");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("9.6", "Application Version");			
			MobileAttributes.put("9.7", "SDK Version");
			MobileAttributes.put("11.4", "Event Type");
			MobileAttributes.put("All-App Usage", "All-App Usage");
			return MobileAttributes;
		}
		if (device.equalsIgnoreCase("App Usage Time")){
			MobileAttributes.put("15.3", "Time In Foreground");	
			MobileAttributes.put("15.1", "Active Time");	
			MobileAttributes.put("15.2", "Total Wait Time");
			MobileAttributes.put("9.5", "Application Name");
			MobileAttributes.put("All-App Usage Time", "All-App Usage Time");
			return MobileAttributes;
		}
		return MobileAttributes;
	}
	
	public static String getOid(String device, String activity ){
		Iterator it = getMobileAttributesByDevice(device).entrySet().iterator();
		boolean found = false;
		String key = "NA";
		while (it.hasNext() && !found) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if (pair.getValue().toString().replaceAll("\\s+","").equalsIgnoreCase(activity.replaceAll("\\s+",""))){
	        	key =pair.getKey().toString();
	        	found = true;
	        }	
	    }
		return key;
	}
	@Test
	public void test() throws Exception {
		System.out.println("key=" + getOid("Mobile Network", "Mobile Carrier"));
	}
		
}

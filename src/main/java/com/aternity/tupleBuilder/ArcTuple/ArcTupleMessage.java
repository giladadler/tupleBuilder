package com.aternity.tupleBuilder.ArcTuple;



import com.aternity.tupleBuilder.utils.RandUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArcTupleMessage {
	
	public static String generateArcTupple (Connection conn, String arcTupple, String globalEpId, String virtualEpId, 
			String accountName, boolean appInternalSection, boolean wifiSection) throws Exception{
		String ACTIVITY_ID ="";
		String APPLICATION_ID ="";
		String APP_TYPE_ID = "";
		String tupleFileName = new java.io.File( "." ).getCanonicalPath() + File.separator + "generic_Freemarker_Arc.json";

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream resource = classloader.getResourceAsStream("generic_Freemarker_Arc.json");

		//InputStream resource = ArcTupleMessage.class.getResourceAsStream("generic_Freemarker_Arc.json");

		File temp = File.createTempFile("generic_With_Freemarker_Arc", ".json");
	    FileUtils.writeStringToFile(temp, IOUtils.toString(resource, "UTF-8"));    
		tupleFileName = temp.getAbsolutePath();
		String jsonFileTransformed ="";
		try {
			Statement statement = conn.createStatement();
			Map<String, String> CONTEXTUALS = new HashMap<String, String>() {};
			Map<String, String> MEASURMENTS = new HashMap<String, String>() {};
			
			String query = "SELECT distinct ma.monitor_type, app.app_type_id, ma.monitor_name as APPLICATION, ma.monitor_id as APPLICATION_ID, ma.activity_name as ACTIVITY,  ma.activity_id,   ma.ma_name, ma.ma_type\n" +
	        "FROM ma_vw ma\n" +
	        "join account on account.account_ID=ma.account_id\n" +
	        "join application app on app.id=ma.monitor_id\n" +
	        " where account.company_name='" + accountName + "' and monitor_name='" + arcTupple.split(",")[1] + "'  and activity_name='" + arcTupple.split(",")[2]
					+"' and monitor_type='" + arcTupple.split(",")[0] + "' \n" +
	        " order by 1 ,3 ,5\n";

			ResultSet result = statement.executeQuery(query);
			int row = 1;
			boolean hasAvailability = false;
			while (result.next()) {
				if (row ==1){
					 APP_TYPE_ID =result.getString("APP_TYPE_ID");
					 APPLICATION_ID =result.getString("APPLICATION_ID");
					 ACTIVITY_ID =result.getString("ACTIVITY_ID");
					 row ++;
				}  
				MEASURMENTS.put(result.getString("MA_NAME"), result.getString("MA_TYPE"));
			 }
			query = "select distinct ma_name, ma_type from ma_vw where monitor_name='" + arcTupple.split(",")[1] +"' and monitor_type='" + arcTupple.split(",")[0] + "'";
			result = statement.executeQuery(query);

			while (result.next()) {				
				MEASURMENTS.put(result.getString("MA_NAME"), result.getString("MA_TYPE"));
			 }
			String queryContextual = "select distinct name, contextual_id , application_name, activity_name from application_view join transaction_contextual "
					+ "on application_view.ACTIVITY_ID=transaction_contextual.transaction_id join monitor mon on mon.id=application_view.application_id "
					+ " where  application_name='" + arcTupple.split(",")[1]  + "' and activity_name='"+ arcTupple.split(",")[2]  + "'"
					+ " and monitor_type='" + arcTupple.split(",")[0] + "'";
			 result = statement.executeQuery(queryContextual);
			while (result.next()) {    //fill in all contextual values
			    //System.out.println("CONTEXTUALS=" +result.getString("NAME")); 
				CONTEXTUALS.put(result.getString("NAME"), result.getString("CONTEXTUAL_ID"));

			 }
			
				Map<String, String> freeMarkerParams = new HashMap<String, String>() {

					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;
				};
				if ( appInternalSection) {
					freeMarkerParams.put("appInternalUserTransactions" , "true");
				}
				if ( wifiSection) {
					freeMarkerParams.put("wifiSection" , "true");
				}
				freeMarkerParams.put("PHYSICAL_ID_PLACE_HOLDER",globalEpId);
				freeMarkerParams.put("VIRTUAL_ID_PLACE_HOLDER", virtualEpId);
				freeMarkerParams.put("MONITOR_ID_PLACE_HOLDER", APPLICATION_ID);
				freeMarkerParams.put("ACTIVITY_ID_PLACE_HOLDER", ACTIVITY_ID);
				freeMarkerParams.put("APP_TYPE_ID_PLACE_HOLDER", APP_TYPE_ID);
				if (arcTupple.split("measurements:").length > 0 && !arcTupple.contains("measurements:CONTEXTUALS")){ //There are measurments to be parsed
					int count = 1 ;
					for (String measurement :  arcTupple.split("measurements:")[1].split("CONTEXTUALS:")[0].split(",")){	
						  String measurementKey = measurement.split("=")[0];
		                  String measurementValue = measurement.split("=")[1];
						  if (!measurementKey.equalsIgnoreCase("availability")) {
							  freeMarkerParams.put("MEASURMENT_" + count, MEASURMENTS.get(measurementKey));
							  //System.out.println("MEASURMENT_=" +  MEASURMENTS.get(measurementKey));
							//special care for Skype MOS ma
							  if (measurementValue.equalsIgnoreCase("[value]") && (measurementKey.contains(" MOS") || measurementKey.contains("Inbound Degradation"))){
								  measurementValue = String.valueOf(RandUtil.randInt(0, 4)) + "." + String.valueOf(RandUtil.randInt(0, 100));
							  }
							  measurementValue = measurementValue.equalsIgnoreCase("[value]") ? String.valueOf(RandUtil.randInt(10, 30)) : measurementValue;
							  freeMarkerParams.put("MEASURMENT_" +count +"_VALUE", measurementValue);
							  
							  count ++;
						  } else {// this value is of type availability
							  freeMarkerParams.put("Availability", measurementValue);
		                  }
					}
				}
				
//				if (! freeMarkerParams.containsKey("Availability")  && !getHasAvailability(conn, arcTupple.split(",")[0]).equalsIgnoreCase("0")){
//					freeMarkerParams.put("DEFAULT_AVAILABILITY", "true");
//					  System.out.println("Using DEFAULT_AVAILABILITY value 1;4");
//				}
				if (arcTupple.substring(arcTupple.indexOf("CONTEXTUAL")).contains("=")){ //There are contextuals to be parsed
					int count = 1 ;
					for (String contextual :  arcTupple.split("CONTEXTUALS:")[1].split(",")){
						freeMarkerParams.put("CONTEXTUAL_" + count, CONTEXTUALS.get(contextual.split("=")[0]));
						String contextualValue = contextual.split("=")[1];
						contextualValue = contextualValue.equalsIgnoreCase("[value]") ? RandUtil.randomString(RandUtil.randInt(3, 12)) : contextualValue.replaceAll("&&&", ",");
						freeMarkerParams.put("CONTEXTUAL_" +count +"_VALUE", contextualValue);
						count ++;
					}
				}
				
				long twoHoursInMillis = 2 * 60 * 60 * 1000;
				freeMarkerParams.put(
						"TIMESTAMP_PLACE_HOLDER",
						String.valueOf(System.currentTimeMillis()
								- twoHoursInMillis));
				jsonFileTransformed = new TestResource(tupleFileName).getAbsolutePath(freeMarkerParams);
		} catch (Exception e) {
			jsonFileTransformed = "Got exception when trying find Monitor values. \n\n\n Check DB connection !!! =>"+e.toString();
		}			
        //System.out.println("Json is=" + FileUtils.readFileToString(new File(jsonFileTransformed)));
    	return jsonFileTransformed;
	}

	private static String getHasAvailability(Connection conn, String name) throws SQLException{
		Statement statement = conn.createStatement();
		ResultSet measurmentResult = statement.executeQuery("select COUNT(*) from application_view where MA_TYPE_NAME='Availability'  and APP_TYPE_NAME ='" + name +"'");
        String response ="";
        while (measurmentResult.next()) {
        	     response = measurmentResult.getString("COUNT(*)");        		 
         }
        return response;
	}
	

}

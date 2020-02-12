package com.aternity.tupleBuilder.ArcTuple;


import com.aternity.tupleBuilder.utils.RandUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * This class acts as Generator for all posible monitors and activities in format applicable for Arc tuple generation.
 * It return a HashMap<String, HashMap<String,String>> where key represent the Arc tuple string and hash-map with ALL values
 * 
 * @author gilada
 * @since   03-08-2015
 * 
 *
 */
public class AllMonitorsArcGenerator {
	


	public static HashMap<String,String> contextualValues = new HashMap<String,String>(){};
	public static HashMap<String,String> measurmentValues = new HashMap<String,String>(){};
	public static String  [] availability = {"GenericPlugin" , "ClientServer", "Web Page Monitoring" , "DNS"};
	
	
	public static HashMap<String, String> getAllMeasurmentHash(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {
		return getAllMeasurment(accountName,DatabseURLConnection, userDB, passwordDB);
	}
	
	public static HashMap<String, String> getAllMeasurmentKeys(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {
		getAllMeasurment(accountName,DatabseURLConnection, userDB, passwordDB);
		return measurmentValues;
	}
	public static HashMap<String, String> getAllContextualHash(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {		
		return getContextuals(getConnection(DatabseURLConnection, userDB, passwordDB));
	}
	public static HashMap<String, String> getAllContextualKeys(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {		
		getContextuals(getConnection(DatabseURLConnection, userDB, passwordDB));
		return contextualValues;
	}
	public static HashMap<String, HashMap<String,String>> getAllMonitorsKeys(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {
		HashMap<String,HashMap<String,String>> allMonitors = new HashMap<String,HashMap<String,String>>(){};
		String query = getAllMonitorsQuery(accountName);
		String appType ="";
		String appTypeID ="";
		String application ="";
		String applicationID ="";
		String activity ="";
		String activityID ="";
		
		 Statement statement = getStatement(DatabseURLConnection, userDB, passwordDB);
		 ResultSet result = statement.executeQuery(query);
		 while (result.next()) {
			appType = result.getString("MONITOR_TYPE");
			appTypeID = result.getString("APP_TYPE_ID");
        	application = result.getString("APPLICATION");
        	applicationID = result.getString("APPLICATION_ID");
        	activity = result.getString("ACTIVITY");
        	activityID = result.getString("ACTIVITY_ID");
        	
        	HashMap<String, String> values = new HashMap<String, String>() {};
        	values.put("APP_TYPE_ID_PLACE_HOLDER", appTypeID);
        	values.put("ACTIVITY_ID_PLACE_HOLDER", activityID);
        	values.put("MONITOR_ID_PLACE_HOLDER", applicationID);
        	
        	allMonitors.put(appType + "," + application + "," + activity, values);
		 }
		return allMonitors;
	}
	public static HashMap<String, HashMap<String,String>> getAllMonitorsMeasurmentAndContextuals(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception {
		
		String appType ="";
		String appTypeID ="";
		String application ="";
		String applicationID ="";
		String activity ="";
		String activityID ="";
		String measurment ="";
		String measurmentID ="";
		String contextual = "";
		HashMap<String, HashMap<String,String>> cacheTupleData = new HashMap<String, HashMap<String,String>>(){};
		HashMap<String,String> monitorsWithmeasurments = getAllMeasurment(accountName,DatabseURLConnection, userDB, passwordDB);
		HashMap<String,String> monitorsWithContextual = getContextuals(getConnection(DatabseURLConnection, userDB, passwordDB));

		

		HashMap<String, String> values = new HashMap<String, String>() {};
		String query = getAllMonitorsQuery(accountName);
	
		 Statement statement = getStatement(DatabseURLConnection, userDB, passwordDB);
		 ResultSet result = statement.executeQuery(query);
		 while (result.next()) {
				appType = result.getString("MONITOR_TYPE");
				appTypeID = result.getString("APP_TYPE_ID");
	        	application = result.getString("APPLICATION");
	        	applicationID = result.getString("APPLICATION_ID");
	        	activity = result.getString("ACTIVITY");
	        	activityID = result.getString("ACTIVITY_ID");
	        	measurment = result.getString("MEASURMENT"); 
	        	measurmentID  = result.getString("MONITORED_ATTR_TYPE_ID"); 
	        	String key = appType + "," + application + "," + activity ;
	        	String keyID = appTypeID + "," + applicationID + "," + activityID ;
	        	contextual = getContextualsPerApplication(key.split(",")[1],key.split(",")[2], monitorsWithContextual);
	        	
	        	String measurments = monitorsWithmeasurments.get(key);
	        	
	        //	System.out.println("key=" + key);
	        //	System.out.println("measurments=" + measurments);
	        	
	        	values = new HashMap<String, String>() {};
	        	values.put("APP_TYPE_ID_PLACE_HOLDER", appTypeID);
	        	values.put("ACTIVITY_ID_PLACE_HOLDER", activityID);
	        	values.put("MONITOR_ID_PLACE_HOLDER", applicationID);
	        	if ( contextual!= null && contextual.contains("=")){
	        		int count = 1 ;
		        	for (String contex : contextual.split(",")){
		        		values.put("CONTEXTUAL_" + count , contextualValues.get(contex.split("=")[0]));
						String contextualValue = contex.split("=")[1];
						contextualValue = contextualValue.equalsIgnoreCase("[value]") ? RandUtil.randomString(RandUtil.randInt(3, 12)) : contextualValue;
						values.put("CONTEXTUAL_" +count +"_VALUE", contextualValue);
						count ++;
		        	}
	        	}
	        	if ( measurments!=null && measurments.contains("=")){
	        		int count = 1 ;
		        	for (String measur : measurments.split(",")){
		        		values.put("MEASURMENT_" + count , measurmentValues.get(measur.split("=")[0]));
						values.put("MEASURMENT_" +count +"_VALUE", String.valueOf(RandUtil.randInt(10, 30)));
						count ++;
		        	}
	        	}
	        	
	        	if (Arrays.asList(availability).contains(appType)){
	        		values.put("AVAILABILITY", "true");
				}
	        	if (!cacheTupleData.containsKey(key+ ",measurements:" + measurments + ",CONTEXTUALS:" + contextual)){
	        		cacheTupleData.put(key+ ",measurements:" + measurments + ",CONTEXTUALS:" + contextual, values);
	        	}    	
	         }
		return cacheTupleData;
	}
	private static String getAllMonitorsQuery(String accountName) {
		String query = "select distinct mon.monitor_type ,app.APP_TYPE_ID ,app.name as application, tr.application_id ,  tr.name as activity, tr.id as activity_id, ma.name as measurment, ma.monitored_attr_type_id  from application app "
				+ "join transaction tr on app.id=tr.application_id join monitored_attr ma on tr.id=ma.transaction_id join monitor mon on mon.id=app.id "
				+ "  join account on account.account_ID=mon.account_id where account.company_name='" + accountName + "'    order by 1 ,3 ,5";
		return query;
	}
	

	private static String getContextualsPerApplication( String app, String activity, HashMap<String,String> monitorsWithContextual) throws SQLException{
		return monitorsWithContextual.get(app + "," + activity)!= null ? monitorsWithContextual.get(app + "," + activity) : "";
	}
	private static Statement getStatement(String DatabseURLConnection,String userDB, String passwordDB)  {		
		try {
			Connection conn;
			conn = getConnection(DatabseURLConnection, userDB,  passwordDB);
			 Statement statement = conn.createStatement();
			 return statement;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		 
		
	}
	
	private static HashMap<String,String> getAllMeasurment(String accountName, String DatabseURLConnection ,String userDB, String passwordDB) throws Exception{
		String appType ="";
		String application ="";
		String activity ="";
		String measurment ="";
		HashMap<String,String> monitorsWithmeasurments = new HashMap<String,String>();
		  String query  = "select mon.monitor_type ,app.name as application ,   tr.name as activity, ma.name as measurment , ma.config as contextual from application app join transaction tr on app.id=tr.application_id join" + 
                          " monitored_attr ma on tr.id=ma.transaction_id join monitor mon on mon.id=app.id join account on account.account_ID=mon.account_id  where account.company_name='"  +accountName + "' "
                          		+ "and  ma.name not like '%deleted%' ";
		  Statement statement = getStatement(DatabseURLConnection, userDB, passwordDB);
		  ResultSet result = statement.executeQuery(query);
	        while (result.next()) {
	        	appType = result.getString("MONITOR_TYPE");
	        	application = result.getString("APPLICATION");
	        	activity = result.getString("ACTIVITY");
	        	measurment = result.getString("MEASURMENT");
	     
	            String key = appType + "," + application + "," + activity;
	        	if (measurment != null) {
	        		 if (monitorsWithmeasurments.get(key)== null ){
	        			 monitorsWithmeasurments.put(key ,  measurment + "=[VALUE]");
		        	} else if (!monitorsWithmeasurments.get(key).contains(measurment)){
		        		monitorsWithmeasurments.put(key , monitorsWithmeasurments.get(key) + "," + measurment + "=[VALUE]");
		        	}
		        }
	        	
	       }
	        
	        query  = "select distinct name, monitored_attr_type_id from monitored_attr order by 1";
	        statement = getStatement(DatabseURLConnection, userDB, passwordDB);
	         result = statement.executeQuery(query);
	      while (result.next()) {
	    	  measurmentValues.put(result.getString("NAME"), result.getString("MONITORED_ATTR_TYPE_ID"));
	      }
	      return monitorsWithmeasurments;
	}

	private static HashMap<String,String> getContextuals(Connection conn) throws SQLException{
		Statement statement = conn.createStatement();
		HashMap<String,String> monitorsWithContextual = new HashMap<String,String>();
		String query = "select distinct name, contextual_id , application_name, activity_name from application_view join transaction_contextual "
				+ "on application_view.ACTIVITY_ID=transaction_contextual.transaction_id join monitor mon on mon.id=application_view.application_id order by 3,4";
		ResultSet result = statement.executeQuery(query);
        while (result.next()) {
        	contextualValues.put(result.getString("name"), result.getString("contextual_id"));     		 
         }
        query = "select distinct  application_name, activity_name ,  name from application_view join transaction_contextual on application_view.ACTIVITY_ID=transaction_contextual."
        		+ "transaction_id join monitor mon on mon.id=application_view.application_id order by 1,2";
        ResultSet result1 = statement.executeQuery(query);
        while (result1.next()) {
        	String key = result1.getString("application_name") + "," + result1.getString("activity_name");
        	if (monitorsWithContextual.get(key)== null ){
        		monitorsWithContextual.put(key ,  result1.getString("name") + "=[VALUE]");
          	} else {
          		monitorsWithContextual.put(key , monitorsWithContextual.get(key) + "," + result1.getString("name") + "=[VALUE]");
          	}
        }
        return monitorsWithContextual;
	}  
	private static Connection getConnection(String DatabseURLConnection,String userDB, String passwordDB) throws Exception {

	        Connection connection = null;
	        try {
	            // Load the JDBC driver
	            String driverName = "oracle.jdbc.driver.OracleDriver";
	            Class.forName(driverName);
                String url = "jdbc:oracle:thin:@" + DatabseURLConnection;
	            connection = DriverManager.getConnection(url, userDB, passwordDB);
	        } catch (ClassNotFoundException e) {
	            System.out.println("getConnection:ClassNotFoundException" +e.getMessage()); 
	            // Could not find the database driver
	        } catch (SQLException e) {
	            System.out.println("getConnection:SQLException" +e.getMessage());   
	        }

	        return connection;
	    }
}

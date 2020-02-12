package com.aternity.tupleBuilder.mobile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MobileMonitors {
	
	public static List<String> getMonitors(String epmIP, String userDB, String passwordDB) throws Exception {
		List<String>  response = new ArrayList<String>(); 
		Statement statement = getStatement(epmIP, userDB, passwordDB);
		String query = "select PRETTY_NAME from MONITOR where PRETTY_NAME like '%(mobile)%'";

		ResultSet result = statement.executeQuery(query);
		while (result.next()) {
			if (!result.getString("PRETTY_NAME").contains("deleted on")){
				response.add(result.getString("PRETTY_NAME"));
			}
		}
		return response;
	}
	public static String getActivities (String epmIP, String userDB, String passwordDB, String monitor) throws Exception {
		StringBuilder  response = new StringBuilder();
		response.append("[");
		Statement statement = getStatement(epmIP, userDB, passwordDB);
		String query = "select * from ACTIVITY join MONITOR on ACTIVITY.monitor_id=MONITOR.id where MONITOR.PRETTY_NAME like '%" + monitor + "%'";
		ResultSet result = statement.executeQuery(query);
		while (result.next()) {
			if (!result.getString("PRETTY_NAME").contains("deleted on")){
				response.append("\"" + result.getString("PRETTY_NAME") + "(");
				String agentCfg = result.getString("AGENT_CFG") ;
				String packageName = getPackage(agentCfg, "mobile:PackageName");
				String activity = getPackage(agentCfg, "mobile:Match");
				response.append(activity + "," + packageName + ")");
				response.append("\",");
			}			
		}
		return response.substring(0, response.length() - 1) + "]";
	}
	
	private static String getPackage (String xml, String nodeMatch) throws Exception{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xml));

	    Document doc = db.parse(is);
	    NodeList nodes = doc.getElementsByTagName(nodeMatch);
	    Element e = (Element)nodes.item(0);
		return  e.getAttribute("value");
	}
	private static Statement getStatement(String epmIP, String userDB, String passwordDB) {
		try {
			Connection conn;
			conn = getConnection(epmIP, userDB, passwordDB);
			Statement statement = conn.createStatement();
			return statement;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	public static Connection getConnection(String epmIp, String dbUser, String dbPassword) throws Exception {

		Connection connection = null;
		try {
			// Load the JDBC driver
			String driverName = "oracle.jdbc.driver.OracleDriver";
			Class.forName(driverName);
			String url = "jdbc:oracle:thin:@" + epmIp;
			connection = DriverManager.getConnection(url, dbUser, dbPassword);
		} catch (ClassNotFoundException e) {
			System.out.println("getConnection:ClassNotFoundException" + e.getMessage());
			// Could not find the database driver
		} catch (SQLException e) {
			System.out.println("getConnection:SQLException" + e.getMessage());
		}

		return connection;
	}

}

package com.aternity.tupleBuilder.mobile;

import com.aternity.tupleBuilder.controller.ArcTupleBuilder;
import com.aternity.tupleBuilder.utils.DBUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MobileDeviceAttributes {
	
	public static LinkedHashMap getMobileAttributesForIndex (String dbcon, String userDB, String passwordDB)throws Exception {
		LinkedHashMap<String, String> MobileAttributes = new LinkedHashMap<String, String>();
		Statement statement = DBUtil.getStatement(dbcon, userDB, passwordDB);
		String query = "select distinct oid,name from STATIC_ATTR where OID is not null order by name";

		ResultSet result = statement.executeQuery(query);
		while (result.next()) {
			MobileAttributes.put(result.getString("oid"), result.getString("name"));
		}

	return MobileAttributes;
	}

	public static LinkedHashMap getMobileAttributes (String dbcon, String userDB, String passwordDB)throws Exception {
		LinkedHashMap<String, String> MobileAttributes = new LinkedHashMap<String, String>();
		Statement statement = DBUtil.getStatement(dbcon, userDB, passwordDB);
		String query = "select distinct oid,name from STATIC_ATTR where OID is not null order by name";

		ResultSet result = statement.executeQuery(query);
		while (result.next()) {
			MobileAttributes.put(result.getString("name"), result.getString("oid"));
		}

		return MobileAttributes;
	}


}

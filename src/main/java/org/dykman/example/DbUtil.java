package org.dykman.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DbUtil {

	public static int getSequenceValue(Connection c,String sequence) 
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(sequence).append(".nextval from dual");
		Statement s = c.createStatement();
		ResultSet rs = s.executeQuery(sb.toString());
		int r = -1;
		if(rs.next()) {
			r = rs.getInt(1);
		}
		s.close();
		return r;
	}
	
	public static boolean insert(Connection c,String table,Map<String,Object> data) 
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table).append(" (");
		for(Map.Entry<String, Object> entry:data.entrySet()) {
			boolean first = true;
			if(first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(entry.getKey());
		}		
		sb.append(") VALUES(");
		boolean first = true;
		for(int i = 0; i < data.size(); ++i) {
			if(first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append("?");
			
		}
		sb.append(")");
		PreparedStatement ps = c.prepareStatement(sb.toString());
		int i = 1;
		for(Map.Entry<String, Object> entry:data.entrySet()) {
			ps.setObject(i, entry.getValue());
			++i;
		}
		ps.executeUpdate();
		ps.close();
		return true;
	}
	
	public static int update(Connection c,String table, Map<String,Object> keys,Map<String,Object> data) 
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(table).append(" SET ");
		boolean first = true;
		for(Map.Entry<String, Object> entry:data.entrySet()) {
			if(first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(entry.getKey()).append(" = ?");
		}
		sb.append(" WHERE ");
		first = true;
		for(Map.Entry<String, Object> entry:keys.entrySet()) {
			if(first) {
				first = false;
			} else {
				sb.append(" AND ");
			}
			sb.append(entry.getKey()).append(" = ?");
		}
		PreparedStatement ps = c.prepareStatement(sb.toString());
		int i = 1;
		for(Map.Entry<String, Object> entry:data.entrySet()) {
			ps.setObject(i, entry.getValue());
			++i;
		}		
		for(Map.Entry<String, Object> entry:keys.entrySet()) {
			ps.setObject(i, entry.getValue());
			++i;
		}
		int result = ps.executeUpdate();
		ps.close();
		return result;
	}
	
	public static List<Map<String,Object>> query(Connection c,String table, Map<String,Object> keys)
		throws SQLException {
		return query(c,table,keys,null);
	}
	
	public static List<Map<String,Object>> query(Connection c,String table, Map<String,Object> keys, List<String> columns) 
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if(columns == null) {
			sb.append("* ");
		} else {
			boolean first = true;
			for(String col:columns) {
				if(first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(col);
			}
		}
		sb.append(" FROM ").append(table).append(" WHERE ");
		boolean first = true;
		for(Map.Entry<String, Object> entry:keys.entrySet()) {
			if(first) {
				first = false;
			} else {
				sb.append(" AND ");
			}
			sb.append(entry.getKey()).append(" = ?");
		}
		PreparedStatement ps = c.prepareStatement(sb.toString());
		int i = 1;
		for(Map.Entry<String, Object> entry:keys.entrySet()) {
			ps.setObject(i, entry.getValue());
		}
		ResultSet rs = ps.executeQuery();
		List<Map<String,Object>> result = getResults(rs);
		ps.close();
		return result;
	}
	
	public static List<Map<String,Object>> getResults(ResultSet rs) 
			throws SQLException {
		List<Map<String,Object>> result=new ArrayList<>();
		ResultSetMetaData mdata = rs.getMetaData();
		
		List<String> cols = new ArrayList<>();
		int columnCount = mdata.getColumnCount();
		for(int i = 1; i <= columnCount; ++i) {
			cols.add(mdata.getColumnName(i));
		}
		
		while(rs.next()) {
			Map<String,Object> row = new LinkedHashMap<>();
			for(int i = 1; i <= columnCount; ++i) {
				row.put(cols.get(i-1), rs.getObject(i));
			}
			result.add(row);
		}
		rs.close();
		return result;
	}
	static ObjectMapper objectMapper = new ObjectMapper();
	
	public static Object parseJson(String in) throws IOException {
		return objectMapper.readValue(in, Object.class);
	}

	public static String toJson(Object o) throws JsonProcessingException {
		return objectMapper.writeValueAsString(o);
	}

	public static boolean isNow(Timestamp activation, Date now, int day, String alrmTime,String userTz) throws ParseException {
//		TimeZone cz = TimeZone.getDefault();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		userTz = userTz.trim();

		long nowMillis = now.getTime();
		TimeZone tz = TimeZone.getDefault();
		TimeZone userTimezone;
		if(userTz.startsWith("UTC") || userTz.startsWith("GMT")) {
			String s = userTz.substring(3).trim();
			userTimezone = TimeZone.getTimeZone(ZoneId.of(s));
		} else if(userTz.charAt(0) == '+' || userTz.charAt(0) == '-' ) {
			userTimezone = TimeZone.getTimeZone(ZoneId.of(userTz));
		} else if(Character.isDigit(userTz.charAt(0))) {
			userTimezone = TimeZone.getTimeZone(ZoneId.of("+"+userTz));
		} else {
			userTimezone = tz;
		}
		

		int offset = tz.getOffset(nowMillis);
		int userOffset= userTimezone.getOffset(nowMillis);
		long userTime = nowMillis-(offset-userOffset);
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		format2.setTimeZone(userTimezone);
		
//		System.out.println(String.format("Offset %d, userOffset %d, usertime %d", offset,userOffset, userTime));
//		System.out.println(String.format("tz %s, userTz %s", tz.getDisplayName(),userTimezone.getDisplayName()));
//		System.out.println(String.format("%d-(%d-%d)", nowMillis, offset, userOffset));
//		
//		System.out.println(String.format("user time %s (%s)", format.format(new Date(userTime)),userTz));
		
		Calendar eventCalendar = Calendar.getInstance(tz);
		Date activationDate = new Date(activation.getTime());//format.parse(activation);
//		System.out.println(String.format("activation %s", format.format(new Date(eventCalendar.getTimeInMillis()))));
		eventCalendar.setTime(activationDate);
		eventCalendar.set(Calendar.SECOND,0);
		eventCalendar.set(Calendar.MILLISECOND,0);
		eventCalendar.set(Calendar.HOUR_OF_DAY,0);
		eventCalendar.set(Calendar.MINUTE,0);
//		System.out.println(String.format("event cleared %s", format.format(new Date(eventCalendar.getTimeInMillis()))));
		
		String[] alarm = alrmTime.split("[:]");
		if(day>0) eventCalendar.add(Calendar.DAY_OF_MONTH, day);
//		System.out.println(String.format("event day %s", format.format(new Date(eventCalendar.getTimeInMillis()))));
		eventCalendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(alarm[0]));
		eventCalendar.set(Calendar.MINUTE,Integer.parseInt(alarm[1]));
//		System.out.println(String.format("event time %s", format.format(new Date(eventCalendar.getTimeInMillis()))));

//		System.out.println(String.format("CALC ==>> %d - %d.. diff = %d ",eventCalendar.getTimeInMillis() , userTime,(eventCalendar.getTimeInMillis() - userTime)));
		if( Math.abs(eventCalendar.getTimeInMillis() - userTime) <= (15*60000)) {
			return true;
		}
		return false;
	}
}

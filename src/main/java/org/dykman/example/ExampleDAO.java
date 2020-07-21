package org.dykman.example;

import static org.dykman.example.DbUtil.parseJson;
import static org.dykman.example.DbUtil.toJson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleDAO {
	static Logger LOG = LoggerFactory.getLogger(ExampleService.class);
public Object getAnswers(Connection c, int surveyId, String subject_name) throws SQLException {
	
	

		
		Map<String, Object> result = null;
		String sql = "select id,userId,surveyId,answers,created,modified,canceled from answers where surveyId = ? and userId=?";
		
		try(PreparedStatement ps = c.prepareStatement(sql)) {
			
			ps.setInt(1, surveyId);
			ps.setString(2, subject_name);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				result = makeAnswer(rs);
			}
			return result;
		} catch(Exception e) {
			LOG.warn(String.format("error while getting answers: %s",e.getLocalizedMessage()));
			return result;
		}	finally {}
	}
	
	public List<Object> getAllAnswers(Connection c) throws SQLException {
	
	String sql = "select id,userId,surveyId,answers,created,modified,canceled from answers";
	List<Object> l = new ArrayList();
	
	try(PreparedStatement ps = c.prepareStatement(sql)) {
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			l.add(makeAnswer(rs));
		}
		return l;
	} catch(Exception e) {
		LOG.warn(String.format("error while getting answers: %s",e.getLocalizedMessage()));
		return l;
	}	finally {}
}

	public int addAnswers(Connection c, int surveyId, Object answers, String userId, Timestamp surveyDate) throws Exception {
		
		String sql = "insert into answers (userid, surveyid, answers, created, modified) values (?, ?, ?, ?, systimestamp)";
		try(PreparedStatement ps = c.prepareStatement(sql, new String[]{"id"})) {
			
			ps.setString(1, userId);
			ps.setInt(2, surveyId);
			ps.setString(3, toJson(answers));
			ps.setTimestamp(4, surveyDate);
			
			int rs = ps.executeUpdate();
			if(rs > 0) {
				ResultSet rkeys = ps.getGeneratedKeys();
				if(rkeys.next()) {
					return rkeys.getInt(1);
				}
			}
			return 0;
			
		} finally {}
	}


	public boolean putAnswers(Connection c, int answerId, Object answers, int surveyId, String userId) throws Exception {
		String sql  = "update answers set answers=?, modified=systimestamp  where id = ? and userId=? and surveyId = ?";
		try(PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, toJson(answers));
			ps.setInt(2, answerId);
			ps.setString(3, userId);
			ps.setInt(4, surveyId);
			return 0 < ps.executeUpdate();
		} finally {}
	}
	
	public boolean patchAnswers(Connection c, int answerId, Object answers, int surveyId, String userId) throws Exception {
		String sql  = "update answers set answers=?, modified=systimestamp  where id = ? and userId=? and surveyId = ?";
		try(PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, toJson(answers));
			ps.setInt(2, answerId);
			ps.setString(3, userId);
			ps.setInt(4, surveyId);
			return 0 < ps.executeUpdate();
		} finally {}
	}
	
	public boolean cancelAnswers(Connection c, int answerId, int surveyId, String userId) throws Exception {
		String sql  = "update answers set cancelled=1, modified=systimestamp  where id = ? and userId=? and surveyId = ?";
		try(PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setString(2, userId);
			ps.setInt(3, surveyId);
			return 0 < ps.executeUpdate();
		} finally {}
	}
	
	public boolean deleteAnswers(Connection c, int answerId, int surveyId, String userId) throws SQLException {
		String sql  = "/delete from  answers where id = ? and userId=? and surveyId = ?";
		try(PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setString(2, userId);
			ps.setInt(3, surveyId);
			return 0 < ps.executeUpdate();
			
		} finally {}
	}
	private Map<String, Object> makeAnswer (ResultSet rs)  throws SQLException {
		
		Map<String, Object> answers = new LinkedHashMap<>();
		try {
			answers.put("id", rs.getInt("id"));
			answers.put("userId", rs.getString("userId"));
			answers.put("surveyId", rs.getInt("surveyId"));
			answers.put("answers", parseJson(rs.getString("answers")));
			answers.put("created", rs.getTimestamp("created"));
			answers.put("modified", rs.getTimestamp("modified"));
			answers.put("canceled", rs.getInt("id"));
			
			return answers;
			
		} catch(Exception e) {
			LOG.warn(String.format("error while building answers Obj: %s",e.getLocalizedMessage()));
			return null;
		}
	}
}

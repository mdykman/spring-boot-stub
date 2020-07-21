package org.dykman.example;


import static org.dykman.example.DbUtil.parseJson;
import static org.dykman.example.DbUtil.toJson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import org.dykman.example.springboot.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {
	@Autowired
	private ApplicationContext context;
	@Autowired // let psring priide it
	private ExampleDAO theDao;
	
	// the convention is to declare one of these for every working class
	static Logger LOG = LoggerFactory.getLogger(ExampleService.class);
	private static String SCHEMA = "ediaryadm";
	
	public ExampleService() {
	}
	
	public Object doSomethingToTheUser(Connection c,int userid) throws NotFoundException {
	//	theDao.addAnswers(c, surveyId, answers, userId, surveyDate);
	//	theDao.cancelAnswers(c, answerId, surveyId, userId);
	//	theDao.deleteAnswers(c, answerId, surveyId, userId);
		Map<String,String> m = new HashMap<>();
		m.put("message", "ok");
		return m;
	}

}

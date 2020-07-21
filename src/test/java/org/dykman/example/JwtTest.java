package org.dykman.example;

import java.util.LinkedHashMap;
import java.util.Map;

import org.dykman.example.JwtManager;
import org.dykman.example.springboot.BeanDefinitions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.jsonwebtoken.Claims;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BeanDefinitions.class})


public class JwtTest {

	@Autowired
	ApplicationContext context;
	
	public JwtTest() {
	}
	
	@Test
	public void testJwtCreation() {
		JwtManager jwt = context.getBean(JwtManager.class);
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("subject", "123456");
		map.put("device", "789");
		String token = jwt.createJwt(map);
		assertNotNull(token);
	}
	
	@Test
	public void testClaims() throws Exception {
		JwtManager jwt = context.getBean(JwtManager.class);
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("subject", "123456");
		String token = jwt.createJwt(map);
		Claims claims = jwt.getAllClaimsFromToken(token);
		assertNotNull(claims);
		
	}
	@Test
	public void testClaimsSubject()  throws Exception{
		JwtManager jwt = context.getBean(JwtManager.class);
		Map<String,Object> map = new LinkedHashMap<>();
		String subject = "123456";
		map.put("subject", subject);
		map.put("device", "789");
		map.put("foo", "bar");
		String token = jwt.createJwt(map);
		Claims claims = jwt.getAllClaimsFromToken(token);
		String s = claims.get("subject",String.class);
		assertNotNull(s);
		assertEquals(subject,s);
	}
	@Test
	public void testClaimsInteger()  throws Exception{
		JwtManager jwt = context.getBean(JwtManager.class);
		Map<String,Object> map = new LinkedHashMap<>();
		map.put("number", 1);
		String token = jwt.createJwt(map);
		Claims claims = jwt.getAllClaimsFromToken(token);
		Integer n = claims.get("number",Integer.class);
		assertNotNull(n);
		assertEquals(new Integer(1),n);
	}

	
	@Test
	public void testIsExpired()  throws Exception{
		JwtManager jwt = context.getBean(JwtManager.class);
		Map<String,Object> map = new LinkedHashMap<>();
		String subject = "123456";
		map.put("subject", subject);
		map.put("device", "789");
		map.put("foo", "bar");
		String token = jwt.createJwt(map);
		assertFalse(jwt.isTokenExpired(token));	
	}
}

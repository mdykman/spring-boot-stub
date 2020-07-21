package org.dykman.example;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;

public class JwtManager {
	static Logger LOG = LoggerFactory.getLogger(JwtManager.class);
	private static final Key key = initKey();
	
	public static final String HTTP_HEADER="X-JWT-Auth";
	public static final String HTTP_HEADER_ADMIN="X-Everest-Auth-Admin";
	
	public static final int JWT_EXPIRY_DAYS = 90;
	public JwtManager() {
	}
	
	private static Key initKey() {		
		try {
			InputStream inp = JwtManager.class.getResourceAsStream("/jwt_private_key");
			String s =  IOUtils.toString(inp,"utf-8");
			s = s.replaceAll("[\\s\\n\\r]", "");
			byte[] bb = Base64.getDecoder().decode(s);
			return Keys.hmacShaKeyFor(bb);
		} catch (IOException e) {
			LOG.error("instantiation exception",e);
			throw new BeanCreationException("unable to read signing certificate",e);
		}
	}
	
	public String getHttpHeader() {
		return HTTP_HEADER;
	}

	public String createJwt(Map <String, Object>  obj) {
		long day = JWT_EXPIRY_DAYS * 86400000L;
		return createJwt(obj, day);
	}
	
	public String getUserfromAdminJwt(String adminToken)  throws Exception{
		Claims c = getAllClaimsFromToken(adminToken);
		return c.get("userId",String.class);
	}
	private String createJwt(Map <String, Object> obj, long timeToLiveMs) {
		long nowMs = System.currentTimeMillis();
		long expireMs = nowMs + timeToLiveMs;
		return Jwts.builder().setClaims(obj)
				.setIssuedAt(new Date(nowMs))
				.setExpiration(new Date(expireMs))
				.signWith(key).compact();
	}
	
	boolean isTokenExpired(String token)  throws Exception{
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}
	public Date getExpirationDateFromToken(String token)  throws Exception {
		return getClaimFromToken(token, Claims::getExpiration);
	}
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver)  throws Exception {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
	public Claims getAllClaimsFromToken(String token) throws Exception {
		try{
			return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
	    } catch (Exception  e) {
	        System.out.println("JWT ERROR: " + e.getMessage());
	        throw new MalformedJwtException(e.getMessage());
	    }	
	}
	public String getSubjectfromJwt(String jwt) throws Exception {
		try {
			Claims c = getAllClaimsFromToken(jwt);
			
			// NOTE::  if, instead of 'subjectId', you gave the name 'sub'
			// then you Claims.getSubject() will use that automatically
			// what you are doing works fine though..
			String othersubject = c.getSubject();
			return (String) c.get("subjectId");
		} finally {}
	}
}

package org.dykman.example.springboot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.dykman.example.JwtManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

@Component
@Order(1)

public class AuditFilter implements Filter {
	static final Logger LOG = LoggerFactory.getLogger(AuditFilter.class);
	private String httpHeaderName;

	String schema;

	boolean enabled = true;

	@Autowired
	ApplicationContext context;

	@Autowired
	Environment env;

	@Autowired
	JwtManager jwtManager;

	String tablename ;

	static AtomicInteger errorCounter = new AtomicInteger();

	public AuditFilter() {

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		schema = env.getProperty("DB_USER");
		httpHeaderName = jwtManager.getHttpHeader();
		if (httpHeaderName == null) {
			enabled = false;
			LOG.warn("no HTTP header specified. disabling.");
		} else {
			LOG.info("scanning for http-header " + httpHeaderName);
		}
		String t = "audit_journal";
		if(!env.containsProperty("USE_MYSQL")) {
			tablename = schema + "." + t;
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (enabled) {
			// this evil cast is an ancient evil, because it already is an
			// HttpServletRequest
			// for the 99.99999 % percent of us who use this API for web. Sun forced this
			// design weirdness on us to support imaginary items that never materialized
			HttpServletRequest req = (HttpServletRequest) request;

			String jwtKey = req.getHeader(httpHeaderName);
			// if it has a header, I'm logging it
			if (jwtKey != null) {
				try {
					Claims claims = jwtManager.getAllClaimsFromToken(jwtKey);
					String subject = claims.get("subjectId", String.class);
					String device = claims.get("deviceId", String.class);
					if (subject != null) {
						DataSource ds = context.getBean("datasource", DataSource.class);
						try (Connection c = ds.getConnection()) {
							/// TODO this table does not yet exist
							try (PreparedStatement ps = c.prepareStatement("insert into " + tablename
									+ "(subject,device,method,path) VALUES (?,?,?,?)")) {
								ps.setString(1, subject);
								ps.setString(2, device);
								ps.setString(3, req.getMethod());
								ps.setString(4, req.getRequestURL().toString());
								ps.executeUpdate();
							}
						} catch (SQLException e) {
							errorCounter.incrementAndGet();
							LOG.error(String.format("failed to journal request for subject %s, device %s because %s",
									subject, device, e.getLocalizedMessage()));
						}
					}
				} catch (Exception e) {
					LOG.error("AUDIT: error parsing jwt token");
				}

			}
		}
		// crude but effective for dev
		if (errorCounter.get() > 10) {
			LOG.warn("error threshold exceeeded, disabling AuditJournal");
			enabled = false;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

}

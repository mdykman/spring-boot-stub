package org.dykman.example.springboot;

import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.dykman.example.ExampleService;
import org.dykman.example.JwtManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;

import io.jsonwebtoken.Claims;


// you can have as many @Configuration classes as you like
// just make sure the packages are listed in the @ComponentScan annotation in the Application class
@Configuration
public class BeanDefinitions {

	Logger LOG = LoggerFactory.getLogger(BeanDefinitions.class);
	// @Autowired
	// private ApplicationContext context;

	public BeanDefinitions() {
		// TODO Auto-generated constructor stub
	}




	@Bean("metricRegistry")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public MetricRegistry metricRegistry() {
		MetricRegistry metrics = new MetricRegistry();
		JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
		reporter.start();
		return metrics;
	}

	@Bean(name = "jwttoken")
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.NO)
	public Claims jtwToken(JwtManager jwtManager) throws Exception {
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String token = curRequest.getHeader(jwtManager.getHttpHeader());
		if (token == null)
			return null;
		Date expiryDate = jwtManager.getExpirationDateFromToken(token);
		if (expiryDate != null && new Date().before(expiryDate)) {
			return null;
		}
		return jwtManager.getAllClaimsFromToken(token);
	}

	@Bean(name = "datasource", destroyMethod = "close")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	// @Lazy
	public DataSource dataSource(MetricRegistry mr) {

		HikariDataSource ds = new HikariDataSource();
		System.out.println(String.format("initializing native the datasource"));
		String user = System.getenv("DB_USER");
		String password = System.getenv("DB_PASSWORD");
		String host = System.getenv("DB_HOST");
		String port = System.getenv("DB_PORT");
		String schema = System.getenv("DB_SCHEMA");

		if (System.getenv("USE_MYSQL") != null) {
			if (port == null)
				port = "3306";
			MysqlDataSource mds = new MysqlDataSource();

			mds.setUser(user);
			mds.setPassword(password);
			mds.setURL("jdbc:mysql://" + host + ":" + port + "/" + schema);
			ds.setDataSource(mds);
		}
		ds.setMinimumIdle(2);
		ds.setMaximumPoolSize(10);

		ds.setLeakDetectionThreshold(120000);
		ds.setMaxLifetime(300000);
		ds.setIdleTimeout(10000);
		ds.setIsolateInternalQueries(true);
		ds.setMetricRegistry(mr);
		return ds;
	}
}

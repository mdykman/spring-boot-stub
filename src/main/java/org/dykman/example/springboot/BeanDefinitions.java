package org.dykman.example.springboot;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;



// you can have as many @Configuration classes as you like
// just make sure the packages are listed in the @ComponentScan annotation in the Application class
@Configuration
public class BeanDefinitions {

	Logger LOG = LoggerFactory.getLogger(BeanDefinitions.class);
	// @Autowired
	// private ApplicationContext context;


	@Bean("metricRegistry")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public MetricRegistry metricRegistry() {
		MetricRegistry metrics = new MetricRegistry();
		JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
		reporter.start();
		return metrics;
	}


	@Bean(name = "datasource", destroyMethod = "close")
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	// @Lazy
	public DataSource dataSource(MetricRegistry mr) {

		HikariConfig hc = new HikariConfig();
		System.out.println(String.format("initializing native the datasource"));
		String user = System.getenv("DB_USER");
		String password = System.getenv("DB_PASSWORD");
		String host = System.getenv("DB_HOST");
		String port = System.getenv("DB_PORT");
		String schema = System.getenv("DB_SCHEMA");
		if (port == null)
			port = "3306";

		hc.setUsername(user);
		hc.setPassword(password);
		hc.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + schema);
		HikariDataSource ds = new HikariDataSource(hc);
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

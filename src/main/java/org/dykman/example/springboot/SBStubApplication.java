package org.dykman.example.springboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
//import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/// import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


// add any packages in which you intend to declare @Bean or @Service or @Configuration on lcasses or methods 
@ComponentScan({ "com.everest.survey", "com.everest.survey.service" })

@SpringBootApplication
public class SBStubApplication extends SpringBootServletInitializer {

	public SBStubApplication() {
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SBStubApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SBStubApplication.class, args);
	}
}

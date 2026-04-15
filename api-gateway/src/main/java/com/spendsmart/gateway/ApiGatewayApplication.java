package com.spendsmart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		// Create the application instance manually
        SpringApplication app = new SpringApplication(ApiGatewayApplication.class);
        
        // Forcefully set the application type to REACTIVE before it even starts
        app.setWebApplicationType(WebApplicationType.REACTIVE);
        
        // Run the app
        app.run(args);
	}

}

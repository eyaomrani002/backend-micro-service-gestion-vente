package org.ms.eurekadiscoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
@SpringBootApplication
@EnableEurekaServer
public class EurekaDiscoveryserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaDiscoveryserviceApplication.class, args);
	}

}

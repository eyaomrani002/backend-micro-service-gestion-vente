package org.ms.client_service;

import org.ms.client_service.entities.Client;
import org.ms.client_service.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@SpringBootApplication
@EnableFeignClients
public class ClientServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ClientServiceApplication.class, args);
	}

	// package org.ms.client_service;
	@Bean
	CommandLineRunner start(ClientRepository clientRepository, RepositoryRestConfiguration repositoryRestConfiguration) {
	    repositoryRestConfiguration.exposeIdsFor(Client.class);
	    return args -> {
	        if (clientRepository.count() == 0) {
	            clientRepository.save(new Client(null, "Ali", "ali.ms@gmail.com", "123 Rue Exemple"));
	            clientRepository.save(new Client(null, "Mariem", "Mariem.ms@gmail.com", "456 Avenue Test"));
	            clientRepository.save(new Client(null, "Mohamed", "Mohamed.ms@gmail.com", "789 Boulevard Demo"));
	        }
	        for (Client client : clientRepository.findAll()) {
	            System.out.println(client.toString());
	        }
	    };
	}
}
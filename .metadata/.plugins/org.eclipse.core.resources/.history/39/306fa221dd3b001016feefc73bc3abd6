package org.ms.authentificationservice;

import org.ms.authentificationservice.entities.AppRole;
import org.ms.authentificationservice.entities.AppUser;
import org.ms.authentificationservice.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class AuthentificationserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthentificationserviceApplication.class, args);
	}

	@Bean
	CommandLineRunner start(UserService userService) {
		return args -> {
			
			AppUser user1 = new AppUser(null, "user1", "123", new ArrayList<>());
			AppUser user2 = new AppUser(null, "user2", "456", new ArrayList<>());
			userService.addUser(user1);
			userService.addUser(user2);

			AppRole roleUser = new AppRole(null, "USER");
			AppRole roleAdmin = new AppRole(null, "ADMIN");
			userService.addRole(roleUser);
			userService.addRole(roleAdmin);

			userService.addRoleToUser("user1", "USER");
			userService.addRoleToUser("user2", "USER");
			userService.addRoleToUser("user2", "ADMIN");
			
			 for (AppUser appUser : userService.getAllUsers())
				{
				System.out.println("User:"+appUser );
				}
		
		};
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

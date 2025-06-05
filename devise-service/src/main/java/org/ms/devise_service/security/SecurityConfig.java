package org.ms.devise_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	            .requestMatchers("/h2-console/**").permitAll()
	            .requestMatchers("/api/auth/**").permitAll()
	            .requestMatchers(HttpMethod.GET, "/devises/**").permitAll()
	            .requestMatchers(HttpMethod.POST, "/devises/**").permitAll()
	            .requestMatchers(HttpMethod.PUT, "/devises/**").permitAll()
	            .requestMatchers(HttpMethod.DELETE, "/devises/**").permitAll()
	            .anyRequest().authenticated()
	        )
	        .addFilterBefore(new JwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
	    return http.build();
	}
	}
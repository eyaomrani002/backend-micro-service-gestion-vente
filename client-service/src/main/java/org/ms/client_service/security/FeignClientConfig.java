package org.ms.client_service.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Récupère le JWT du contexte de sécurité
            Object auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth instanceof org.springframework.security.core.Authentication) {
                String jwt = (String) ((org.springframework.security.core.Authentication) auth).getCredentials();
                if (jwt != null) {
                    requestTemplate.header("Authorization", "Bearer " + jwt);
                }
            }
        };
    }
}
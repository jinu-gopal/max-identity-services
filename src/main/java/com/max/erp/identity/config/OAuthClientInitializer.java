package com.max.erp.identity.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.UUID;

@Configuration
public class OAuthClientInitializer {

    @Bean
    public CommandLineRunner initializeOAuthClient(
            RegisteredClientRepository registeredClientRepository) {

        return args -> {

            if (registeredClientRepository.findByClientId("max-erp-web") != null) {
                return;
            }

            RegisteredClient client =
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId("max-erp-web")
                            .clientName("MAX ERP Web Application")
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.NONE
                            )
                            .authorizationGrantType(
                                    AuthorizationGrantType.AUTHORIZATION_CODE
                            )
                            .authorizationGrantType(
                                    AuthorizationGrantType.REFRESH_TOKEN
                            )
                            .redirectUri(
                                    "http://127.0.0.1:3000/login/oauth2/code/max-erp"
                            )
                            .scope("openid")
                            .scope("profile")
                            .scope("email")
                            .build();

            registeredClientRepository.save(client);
        };
    }
}
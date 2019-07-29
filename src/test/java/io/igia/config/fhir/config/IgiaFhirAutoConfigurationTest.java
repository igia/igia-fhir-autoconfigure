/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.config.fhir.config;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.Assert;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import io.igia.config.fhir.IgiaFhirAutoConfiguration;
import io.igia.config.fhir.interceptor.ScopeBasedAuthorizationInterceptor;
import io.igia.config.fhir.server.FhirRestfulServerCustomizer;

public class IgiaFhirAutoConfigurationTest {
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(IgiaFhirAutoConfiguration.class));

    @Test
    public void fhirContextExistsTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                FhirContext fhirContext = context.getBean(FhirContext.class);
                Assert.notNull(fhirContext, "FhirContext bean must not be null");
            });
    }

    @Test
    public void fhirRestfulServerCustomizerTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                FhirRestfulServerCustomizer fhirRestfulServerCustomizer = (FhirRestfulServerCustomizer) context
                        .getBean("fhirRestfulServerCustomizer");
                Assert.notNull(fhirRestfulServerCustomizer, "fhirRestfulServerCustomizer bean must not be null");
            });
    }

    @Test
    public void corsInterceptorTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                IServerInterceptor serverInterceptor = (IServerInterceptor) context.getBean(CorsInterceptor.class);
                Assert.notNull(serverInterceptor, "CorsInterceptor bean must not be null");
            });
    }

    @Test
    public void responseHighlighterInterceptorTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                IServerInterceptor serverInterceptor = (IServerInterceptor) context.getBean(ResponseHighlighterInterceptor.class);
                Assert.notNull(serverInterceptor, "ResponseHighlighterInterceptor bean must not be null");
            });
    }

    @Test
    public void loggingInterceptorTrueTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
            		"hapi.fhir.rest.logging.access:true")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                IServerInterceptor serverInterceptor = (IServerInterceptor) context.getBean("loggingInterceptor");
                Assert.notNull(serverInterceptor, "loggingInterceptor bean must not be null");
            });
    }
    
    @Test(expected = NoSuchBeanDefinitionException.class)
    public void loggingInterceptorFalseTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
            		"hapi.fhir.rest.logging.access:false")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                context.getBean("loggingInterceptor");
            });
    }
    
    @Test(expected = NoSuchBeanDefinitionException.class)
    public void loggingInterceptorDefaultTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                context.getBean("loggingInterceptor");
            });
    }

    @Test
    public void stacktraceExceptionHandlingInterceptorTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
                    "hapi.fhir.rest.exception.stacktrace:true")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                IServerInterceptor serverInterceptor = (IServerInterceptor) context.getBean("stacktraceExceptionHandlingInterceptor");
                Assert.notNull(serverInterceptor, "stacktraceExceptionHandlingInterceptor bean must not be null");
            });
    }

    @Test
    public void exceptionHandlingInterceptorTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withPropertyValues(
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
                    "hapi.fhir.rest.exception.stacktrace:false")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                IServerInterceptor serverInterceptor = (IServerInterceptor) context.getBean("exceptionHandlingInterceptor");
                Assert.notNull(serverInterceptor, "exceptionHandlingInterceptor bean must not be null");
            });
    }

    @Test
    public void scopeBasedAuthorizationInterceptorTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withUserConfiguration(SecurityConfig.class)
            .withPropertyValues(
                    "security.oauth2.client.access-token-uri:http://access-token-uri.com",
                    "security.oauth2.client.client-id:client-id",
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
                    "hspc.platform.api.security.mode:secured")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                ScopeBasedAuthorizationInterceptor serverInterceptor = context.getBean(ScopeBasedAuthorizationInterceptor.class);
                Assert.notNull(serverInterceptor, "scopeBasedAuthorizationInterceptor bean must not be null");
            });
    }

    @Test
    public void tokenStoreTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withUserConfiguration(SecurityConfig.class)
            .withPropertyValues(
                    "security.oauth2.client.access-token-uri:http://access-token-uri.com",
                    "security.oauth2.client.client-id:client-id",
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
                    "security.oauth2.resource.jwk.key-uri:http://jwk-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                TokenStore tokenStore = context.getBean(TokenStore.class);
                Assert.notNull(tokenStore, "TokenStore bean must not be null");
            });
    }

    @Test
    public void jwtAccessTokenConverterTest() {
        this.contextRunner
            .withUserConfiguration(PatientResourceProviderConfig.class)
            .withUserConfiguration(SecurityConfig.class)
            .withPropertyValues(
                    "security.oauth2.client.access-token-uri:http://access-token-uri.com",
                    "security.oauth2.client.client-id:client-id",
                    "security.oauth2.resource.jwk.key-set-uri:http://jwk-set-uri.com",
                    "security.oauth2.resource.jwk.key-uri:http://jwk-uri.com")
            .withConfiguration(AutoConfigurations.of(OAuth2AutoConfiguration.class))
            .run((context) -> {
                JwtAccessTokenConverter jwtAccessTokenConverter = context.getBean(JwtAccessTokenConverter.class);
                Assert.notNull(jwtAccessTokenConverter, "JwtAccessTokenConverter bean must not be null");
            });
    }

    @Configuration
    protected static class PatientResourceProviderConfig {

        @Bean
        public IResourceProvider patientProvider() {
            return new PatientResourceProvider();
        }

    }

    @Configuration
    @EnableOAuth2Client
    protected static class SecurityConfig {

        @Bean
        public TokenStore tokenStore() {
            return new JwtTokenStore(null);
        }

        @Bean
        public JwtAccessTokenConverter jwtAccessTokenConverter() {
            return new JwtAccessTokenConverter();
        }

        @Bean
        public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
                OAuth2ProtectedResourceDetails details) {
            return new OAuth2RestTemplate(details, oauth2ClientContext);
        }

        @Bean
        public PrincipalExtractor principalExtractor() {
            class Impl implements PrincipalExtractor {
                @Override
                public Object extractPrincipal(Map<String, Object> map) {
                    return null;
                }
            }
            return new Impl();
        }

        @Bean
        public AuthoritiesExtractor authoritiesExtractor() {
            class Impl implements AuthoritiesExtractor {
                @Override
                public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
                    return null;
                }
            }
            return new Impl();
        }
    }

    public static class PatientResourceProvider implements IResourceProvider {
        @Override
        public Class<? extends IBaseResource> getResourceType() {
            return Patient.class;
        }
    }
}

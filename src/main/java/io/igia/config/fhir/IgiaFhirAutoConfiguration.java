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
package io.igia.config.fhir;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import io.igia.config.fhir.interceptor.IgiaExceptionHandlingInterceptor;
import io.igia.config.fhir.interceptor.ScopeBasedAuthorizationInterceptor;
import io.igia.config.fhir.rest.IgiaFhirController;
import io.igia.config.fhir.server.FhirRestfulServerCustomizer;

@Configuration
@ConditionalOnClass(IgiaFhirController.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableConfigurationProperties(FhirProperties.class)
public class IgiaFhirAutoConfiguration {

	private final Logger log = LoggerFactory.getLogger(IgiaFhirAutoConfiguration.class);

	private final FhirProperties properties;
	private final ResourceServerProperties resourceServerProperties;
	
	public IgiaFhirAutoConfiguration(FhirProperties  properties, ResourceServerProperties resourceServerProperties) {
		
		log.info("Starting FHIR auto configuration");
		
		this.properties = properties;
		this.resourceServerProperties = resourceServerProperties;
	}

	@PostConstruct
    public void init() {
        log.debug("creating FHIR auto configuration done");
	}
	
	@Bean
	@ConditionalOnMissingBean
	public FhirContext fhirContext() {
		FhirContext fhirContext = new FhirContext(properties.getVersion());
		return fhirContext;
	}

	@Bean
	@ConditionalOnMissingBean(name="fhirRestfulServerCustomizer")
	public FhirRestfulServerCustomizer fhirRestfulServerCustomizer() {
		return new IgiaRestfulServerCustomizer (this.properties);
	}

	@Bean
	@ConditionalOnMissingBean
	public CorsInterceptor corsInterceptor() {
		/*
		 * Enable CORS
		 */
		CorsConfiguration config = new CorsConfiguration();
		CorsInterceptor corsInterceptor = new CorsInterceptor(config);
		config.addAllowedHeader("Accept");
		config.addAllowedHeader("Content-Type");
		config.addAllowedOrigin("*");
		config.addExposedHeader("Location");
		config.addExposedHeader("Content-Location");
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		return corsInterceptor;
	}

	@Bean
	@ConditionalOnMissingBean
	public ResponseHighlighterInterceptor responseHighlighterInterceptor() {
		/*
		 * This server interceptor causes the server to return nicely
		 * formatter and coloured responses instead of plain JSON/XML if
		 * the request is coming from a browser window. It is optional,
		 * but can be nice for testing.
		 */
		return new ResponseHighlighterInterceptor();
	}

	@Bean
	@ConditionalOnMissingBean(name="loggingInterceptor")
	@ConditionalOnProperty(value = "hapi.fhir.rest.logging.access", havingValue="true", matchIfMissing = false)
	/**
	 * access log that has details about each incoming request, only created if configured
	 */
	public IServerInterceptor loggingInterceptor() {
		LoggingInterceptor retVal = new LoggingInterceptor();
		retVal.setLoggerName("igia_fhir.access");
		retVal.setMessageFormat(
				"Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
		retVal.setLogExceptions(true);
		retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
		return retVal;
	}

	@Bean
	@ConditionalOnMissingBean(name="stacktraceExceptionHandlingInterceptor")
	@ConditionalOnProperty(value = "hapi.fhir.rest.exception.stacktrace", havingValue="true", matchIfMissing = false)
	public IServerInterceptor stacktraceExceptionHandlingInterceptor() {
		ExceptionHandlingInterceptor retVal = new IgiaExceptionHandlingInterceptor();
		retVal.setReturnStackTracesForExceptionTypes(InternalErrorException.class, InvalidRequestException.class);
		return retVal;
	}

	@Bean
	@ConditionalOnMissingBean(name="exceptionHandlingInterceptor")
	@ConditionalOnProperty(value = "hapi.fhir.rest.exception.stacktrace", havingValue="false", matchIfMissing = true)
	public IServerInterceptor exceptionHandlingInterceptor() {
		ExceptionHandlingInterceptor retVal = new IgiaExceptionHandlingInterceptor();
		return retVal;
	}
	
	@Bean
	@ConditionalOnMissingBean(name="scopeBasedAuthorizationInterceptor")
	@ConditionalOnExpression("'${hspc.platform.api.security.mode}'=='secured' || '${hspc.platform.api.security.mode}'=='mock'")
	public ScopeBasedAuthorizationInterceptor scopeBasedAuthorizationInterceptor(TokenStore tokenStore, OAuth2RestTemplate oAuth2RestTemplate) {
		return new ScopeBasedAuthorizationInterceptor(tokenStore, oAuth2RestTemplate);
	}
	
    @Bean
	@ConditionalOnMissingBean
    @ConditionalOnProperty("security.oauth2.resource.jwt.key-uri")
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
	@ConditionalOnMissingBean
    @ConditionalOnProperty("security.oauth2.resource.jwt.key-uri")
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getKeyFromAuthorizationServer());
        return converter;
    }

    private String getKeyFromAuthorizationServer() {
        return Optional.ofNullable(
            new RestTemplate()
                .exchange(
                    resourceServerProperties.getJwt().getKeyUri(),
                    HttpMethod.GET,
                    new HttpEntity<Void>(new HttpHeaders()),
                    Map.class
                )
                .getBody()
                .get("public_key"))
            .map(publicKey -> String.format("-----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----", publicKey))
            .orElse(resourceServerProperties.getJwt().getKeyValue());
    }
    
    @Configuration
    @Order(1)
    protected static class ResourceServerConfiguration
        extends ResourceServerConfigurerAdapter {
      @Override
      public void configure(HttpSecurity http) throws Exception {
        http
        	.authorizeRequests()
        	.antMatchers("/api/metadata").permitAll(); //FHIR capability statement endpoint should be open    
      }
    }
}

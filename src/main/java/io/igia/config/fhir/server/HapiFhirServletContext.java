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
package io.igia.config.fhir.server;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IServerConformanceProvider;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import io.igia.config.fhir.FhirProperties;

@Component
@ConditionalOnClass(IResourceProvider.class)
@EnableConfigurationProperties(FhirProperties.class)
@ConfigurationProperties("hapi.fhir.rest")
public class HapiFhirServletContext {
	
	private final Logger log = LoggerFactory.getLogger(HapiFhirServletContext.class);
	
	private final FhirProperties properties;
	private final FhirContext fhirContext;
	private final List<IResourceProvider> resourceProviders;       
	private final IServerConformanceProvider<? extends IBaseResource> conformanceProvider;
	private final IPagingProvider pagingProvider;
	private final List<IServerInterceptor> interceptors;
	private final List<FhirRestfulServerCustomizer> customizers;
	
	public HapiFhirServletContext(
			FhirProperties properties,
			FhirContext fhirContext,
			ObjectProvider<List<IResourceProvider>> resourceProviders,
			ObjectProvider<IServerConformanceProvider<? extends IBaseResource>> conformanceProvider,
			ObjectProvider<IPagingProvider> pagingProvider,
			ObjectProvider<List<IServerInterceptor>> interceptors,
			ObjectProvider<List<FhirRestfulServerCustomizer>> customizers) {
		
		log.info("Start igia FHIR Servlet Context");
		
		this.properties = properties;
		this.fhirContext = fhirContext;
		this.resourceProviders = resourceProviders.getIfAvailable();
		this.conformanceProvider = conformanceProvider.getIfAvailable();
		this.pagingProvider = pagingProvider.getIfAvailable();
		this.interceptors = interceptors.getIfAvailable();
		this.customizers = customizers.getIfAvailable();
	}
	
	public FhirProperties getProperties() {
		return properties;
	}

	public FhirContext getFhirContext() {
		return fhirContext;
	}

	public List<IResourceProvider> getResourceProviders() {
		return resourceProviders;
	}

	public IServerConformanceProvider<? extends IBaseResource> getConformanceProvider() {
		return conformanceProvider;
	}

	public IPagingProvider getPagingProvider() {
		return pagingProvider;
	}

	public List<IServerInterceptor> getInterceptors() {
		return interceptors;
	}

	public List<FhirRestfulServerCustomizer> getCustomizers() {
		return customizers;
	}
}

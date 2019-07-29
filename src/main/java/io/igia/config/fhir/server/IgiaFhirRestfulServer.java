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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.boot.context.properties.ConfigurationProperties;

import ca.uhn.fhir.rest.server.RestfulServer;

@ConfigurationProperties("hapi.fhir.rest")
public class IgiaFhirRestfulServer extends RestfulServer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1675936994958105729L;

	private final Logger log = LoggerFactory.getLogger(IgiaFhirRestfulServer.class);
	
	@Autowired
	private HapiFhirServletContext hapiFhirServletContext;

	@Override
	protected void initialize() throws ServletException {
		
		log.info("Initializing igia HAPI FHIR server ..");;
		
		super.initialize();
		
		setFhirContext(this.hapiFhirServletContext.getFhirContext());
		setResourceProviders(this.hapiFhirServletContext.getResourceProviders());
		if (this.hapiFhirServletContext.getConformanceProvider() != null) {
			setServerConformanceProvider(this.hapiFhirServletContext.getConformanceProvider());
		}
		setPagingProvider(this.hapiFhirServletContext.getPagingProvider());
		setInterceptors(this.hapiFhirServletContext.getInterceptors());		

		customize();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
		super.init(config);		
	}

	private void customize() {
		if (this.hapiFhirServletContext.getCustomizers() != null) {
			AnnotationAwareOrderComparator.sort(this.hapiFhirServletContext.getCustomizers());
			for (FhirRestfulServerCustomizer customizer : this.hapiFhirServletContext.getCustomizers()) {
				customizer.customize(this);
			}
		}
	}
}

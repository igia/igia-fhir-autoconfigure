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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import io.igia.config.fhir.server.FhirRestfulServerCustomizer;


public class IgiaRestfulServerCustomizer implements FhirRestfulServerCustomizer {
	private final FhirProperties properties;
	
	private final Logger log = LoggerFactory.getLogger(IgiaRestfulServerCustomizer.class);

	public IgiaRestfulServerCustomizer(FhirProperties properties) {
		
		log.info("create restful server customizer");
		
		this.properties = properties;
	}

	@Override
	public void customize(RestfulServer server) {
		/*
		 * Use a narrative generator.
		 */
		server.getFhirContext().setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
		//set server address strategy
		server.setServerAddressStrategy(new HardcodedServerAddressStrategy(properties.getServer().getPath() + "/*"));
	}
}
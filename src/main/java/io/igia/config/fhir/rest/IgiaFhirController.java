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
package io.igia.config.fhir.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.ServletWrappingController;

import ca.uhn.fhir.rest.server.IResourceProvider;
import io.igia.config.fhir.server.HapiFhirServletContext;
import io.igia.config.fhir.server.IgiaFhirRestfulServer;

@RestController    	
@ConditionalOnClass(IResourceProvider.class)
public class IgiaFhirController extends ServletWrappingController {    		
	private final HapiFhirServletContext hapiFhirServletContext;		
	
	private final Logger log = LoggerFactory.getLogger(IgiaFhirController.class);

	public IgiaFhirController(HapiFhirServletContext hapiFhirServletContext) {	
		
		log.info("Start FHIR RestController");
		
		this.hapiFhirServletContext = hapiFhirServletContext;		

		setServletClass(IgiaFhirRestfulServer.class);
		setServletName("igiaFhirRestfulServerConfiguration");
		setSupportedMethods(
				RequestMethod.GET.toString(),
				RequestMethod.PUT.toString(),
				RequestMethod.POST.toString(),
				RequestMethod.PATCH.toString(),
				RequestMethod.DELETE.toString(),
				RequestMethod.HEAD.toString(),
				RequestMethod.OPTIONS.toString(),
				RequestMethod.TRACE.toString()
				);
	}

	@RequestMapping(value = {
			"${hapi.fhir.server.path}",
			"${hapi.fhir.server.path}/**"
	})
	public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request)
        {
            @Override
            public String getServletPath()
            {
                return hapiFhirServletContext.getProperties().getServer().getPath();
            }
        };
        
		this.handleRequest(wrapper, response);
	}
}

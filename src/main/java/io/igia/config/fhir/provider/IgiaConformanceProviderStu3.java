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
package io.igia.config.fhir.provider;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hspconsortium.platform.api.fhir.repository.MetadataRepositoryConfig;
import org.hspconsortium.platform.api.fhir.repository.MetadataRepositoryStu3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = {"org.hspconsortium.platform.api.fhir.repository", "org.hspconsortium.platform.api.smart"})
@ConditionalOnExpression("'${hspc.platform.api.security.mode}'=='secured' || '${hspc.platform.api.security.mode}'=='mock'")
public class IgiaConformanceProviderStu3 extends ServerCapabilityStatementProvider {
	private static final String CAPABILITIES_EXTENSION_URL = "http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities";
	private MetadataRepositoryStu3 metadataRepository;
	private MetadataRepositoryConfig metadataRepositoryConfig;
	private boolean CONFIGURED = false;
	
	@Value("${hspc.platform.authorization.smart.capabilities:}")    
	private String[] capabilities;
	
	public IgiaConformanceProviderStu3(MetadataRepositoryStu3 metadataRepository, MetadataRepositoryConfig metadataRepositoryConfig) {
		super();
		this.metadataRepository = metadataRepository;
		this.metadataRepositoryConfig = metadataRepositoryConfig;
	}

    public void setMetadataRepository(MetadataRepositoryStu3 metadataRepository) {
        this.metadataRepository = metadataRepository;
    }
    
	@Override
    public CapabilityStatement getServerConformance(HttpServletRequest request) {
        CapabilityStatement capabilityStatement = super.getServerConformance(request);        
        if(!CONFIGURED) {
        		capabilityStatement = this.metadataRepository.addCapabilityStatement(capabilityStatement);
        		capabilityStatement = addCapabilityExtensions(capabilityStatement);
        		CONFIGURED = true;
        }        
        return capabilityStatement;
    }
	
    private CapabilityStatement addCapabilityExtensions(CapabilityStatement capabilityStatement) {
		if(capabilities == null || capabilities.length == 0) {
			return capabilityStatement;
		}
		
        if (metadataRepositoryConfig.isSecured()) {
            List<CapabilityStatement.CapabilityStatementRestComponent> restList = capabilityStatement.getRest();

            CapabilityStatement.CapabilityStatementRestComponent rest = restList.get(0);
            CapabilityStatement.CapabilityStatementRestSecurityComponent restSecurity = rest.getSecurity();    
            
            for (String capability: capabilities)
            {
            		restSecurity.addExtension(new Extension(CAPABILITIES_EXTENSION_URL, new CodeType(capability)));                
            }
        }
        
        return capabilityStatement;
    }

}

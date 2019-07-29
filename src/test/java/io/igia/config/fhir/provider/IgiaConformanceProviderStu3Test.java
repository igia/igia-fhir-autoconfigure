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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.assertj.core.util.Arrays;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Extension;
import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import io.igia.config.fhir.provider.IgiaConformanceProviderStu3;

public class IgiaConformanceProviderStu3Test {
    private static final String CAPABILITIES_EXTENSION_URL = "http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities";
    private static final String HSPC_EXTENSION_URL = "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";
    private static final String[] HSPC_Extension = { "authorize", "token", "register", "launch-registration" };
    private static FhirContext ourCtx;

    private String[] capabilities = { "launch-standalone", "client-public", "client-confidential-symmetric",
                "sso-openid-connect", "context-standalone-patient", "permission-patient", "permission-offline" };

    static {
        ourCtx = FhirContext.forDstu3();
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void conformanceProviderExistsTest() {
        this.contextRunner.withUserConfiguration(ConformanceProviderConfig.class)
            .withPropertyValues("hspc.platform.api.security.mode:secured", "hspc.platform.manifest.override:false")
            .run((context) -> {
                IgiaConformanceProviderStu3 igiaConformanceProviderStu3 = context
                .getBean(IgiaConformanceProviderStu3.class);
                Assert.notNull(igiaConformanceProviderStu3, "IgiaConformanceProviderStu3 bean must not be null");
            });
    }

    @Test
    public void addCapabilityExtensionsTest() {
        this.contextRunner.withUserConfiguration(ConformanceProviderConfig.class)
            .withPropertyValues("hspc.platform.api.security.mode:secured", "hspc.platform.manifest.override:false",
                "hspc.platform.authorization.url: http://keycloak:9080",
                "hspc.platform.authorization.authorizeUrlPath: /auth/realms/master/protocol/openid-connect/auth",
                "hspc.platform.authorization.tokenUrlPath: /auth/realms/master/protocol/smart-openid-connect/token",
                "hspc.platform.authorization.smart.urisEndpointExtensionUrl: http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris",
                "hspc.platform.authorization.smart.registrationEndpointUrlPath: /auth/realms/master/clients-registrations/openid-connect",
                "hspc.platform.authorization.smart.capabilities:launch-standalone, client-public, client-confidential-symmetric, sso-openid-connect, context-standalone-patient, permission-patient, permission-offline")
                .run((context) -> {
                    IgiaConformanceProviderStu3 igiaConformanceProviderStu3 = context
                    .getBean(IgiaConformanceProviderStu3.class);
                    RestfulServer restfulServer = context.getBean(RestfulServer.class);
                    restfulServer.setServerConformanceProvider(igiaConformanceProviderStu3);
                    CapabilityStatement capabilityStatement = igiaConformanceProviderStu3
                    .getServerConformance(createHttpServletRequest());
                    Assert.notNull(capabilityStatement, "capabilityStatement must not be null");
                    AssertHSPCExtension(capabilityStatement);
                    AssertExtendedExtension(capabilityStatement);
                });
    }

    public void AssertHSPCExtension(CapabilityStatement capabilityStatement) {
        List<Extension> extensions = capabilityStatement.getRest().get(0).getSecurity()
        .getExtensionsByUrl(HSPC_EXTENSION_URL).get(0).getExtension();
        Assert.isTrue(extensions.size() == 4,"capabilityStatement must have 3 extensions of url " + HSPC_EXTENSION_URL);
        List<String> availableExtension = new ArrayList<String>();
        for (Extension ext : extensions) {
            String value = ext.getUrl();
            availableExtension.add(value);
        }
        Assert.isTrue(Arrays.asList(HSPC_Extension).containsAll(availableExtension),
        HSPC_Extension + " should be included as extension in capability statement");
    }

    public void AssertExtendedExtension(CapabilityStatement capabilityStatement) {
        List<Extension> extensions = capabilityStatement.getRest().get(0).getSecurity()
        .getExtensionsByUrl(CAPABILITIES_EXTENSION_URL);
        Assert.isTrue(extensions.size() == 7,"capabilityStatement must have 7 extensions of url " + CAPABILITIES_EXTENSION_URL);
        List<String> availableExtension = new ArrayList<String>();
        for (Extension ext : extensions) {
            String value = ext.getValueAsPrimitive().getValueAsString();
            availableExtension.add(value);
        }
        Assert.isTrue(Arrays.asList(capabilities).containsAll(availableExtension),capabilities + " should be included as extension in capability statement");
    }

    @Configuration
    @ComponentScan(basePackages = { "io.igia.config.fhir.provider" })
    protected static class ConformanceProviderConfig {
        @Bean
        RestfulServer restfulServer() {
            return new RestfulServer(ourCtx);
        }
    }

    private HttpServletRequest createHttpServletRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/FhirStorm/fhir/Patient/_search");
        when(req.getServletPath()).thenReturn("/fhir");
        when(req.getRequestURL()).thenReturn(new StringBuffer().append("http://fhirstorm.dyndns.org:8080/FhirStorm/fhir/Patient/_search"));
        when(req.getContextPath()).thenReturn("/FhirStorm");
        return req;
    }
}

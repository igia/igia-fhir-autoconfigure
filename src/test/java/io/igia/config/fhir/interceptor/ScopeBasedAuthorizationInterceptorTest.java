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
package io.igia.config.fhir.interceptor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.igia.config.fhir.IgiafhirapitestApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { IgiafhirapitestApp.class,
		ScopeBasedAuthorizationInterceptorTestUtil.class,
		ScopeBasedAuthorizationInterceptorTestUtil.LocalSecurityBeanOverrideConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test, standalone")
public class ScopeBasedAuthorizationInterceptorTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ScopeBasedAuthorizationInterceptorTestUtil util;
 
    // patient/*.* scope tests
    @Test
    public void testPatientAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupReadPatient("12345");

        mockMvc.perform(get("/api/Patient/12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testPatientForbiddenIdRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("2345");

        mockMvc.perform(get("/api/Patient/2345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientAllowedSearch() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testPatientForbiddenIdSearch() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("2345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|2345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testPatientAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testPatientAllowedDelete() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupDeletePatient("12345");

        mockMvc.perform(delete("/api/Patient/12345").contentType("application/fhir+xml")
                .with(authentication(oauthTestAuthentication)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testPatientForbiddenIdWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/2345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"2345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientForbiddenInstanceOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientInstanceOperation();

        mockMvc.perform(post("/api/Patient/12345/$instance")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(post("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientAllowedByReference() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testPatientForbiddenIdByReference() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("2345");

        mockMvc.perform(get("/api/Observation?patient=Patient/2345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // patient/*.read scope tests
    @Test
    public void testPatientReadAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }

    @Test
    public void testPatientReadForbiddenWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientReadForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientReadForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(post("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // patient/*.write scope tests
    @Test
    public void testPatientWriteForbiddenRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testPatientWriteAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testPatientWriteForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientWriteForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // patient/Patient.* scope tests
    @Test
    public void testPatientResourceAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }

    @Test
    public void testPatientResourceAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testPatientResourceForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceAllowedInstanceOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientInstanceOperation();

        mockMvc.perform(post("/api/Patient/12345/$instance")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testPatientResourceForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientResourceForbiddenResource() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // patient/Patient.read scope tests
    @Test
    public void testPatientResourceReadAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testPatientResourceReadForbiddenIdRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("2345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|2345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceReadForbiddenWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceReadForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceReadForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testPatientResourceReadForbiddenResourceRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // patient/Patient.write scope tests
    @Test
    public void testPatientResourceWriteForbiddenRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceWriteAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testPatientResourceWriteForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatientResourceWriteForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // patient/Observation.* scope tests
    @Test
    public void testResourceAllowedPatientRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testResourceForbiddenPatientRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("2345");

        mockMvc.perform(get("/api/Observation?patient=Patient/2345").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    public void testResourceForbiddenInstanceOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupObservationInstanceOperation();

        mockMvc.perform(post("/api/Observation/observationid/$instance")
            .with(authentication(oauthTestAuthentication)))
        	.andExpect(status().isForbidden());
    }
    
    @Test
    public void testResourceForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupObservationTypeOperation();

        mockMvc.perform(post("/api/Observation/$type")
            .with(authentication(oauthTestAuthentication)))
        	.andExpect(status().isForbidden());
    }
    
    @Test
    public void testResourceForbiddenResourceRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
            .with(authentication(oauthTestAuthentication)))
        	.andExpect(status().isForbidden());
    }
    
    @Test
    public void testResourceForbiddenResourceWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // patient/Observation.read scope tests
    @Test
    public void testResourceReadForbiddenResourceRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // patient/Observation.write scope tests
    @Test
    public void testResourceWriteForbiddenResourceWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("patient/Observation.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // user/*.* scope tests
    @Test
    public void testUserAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupReadPatient("12345");

        mockMvc.perform(get("/api/Patient/12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedIdRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupReadPatient("2345");

        mockMvc.perform(get("/api/Patient/2345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedSearch() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedIdSearch() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("2345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|2345")
        	.with(authentication(oauthTestAuthentication)))
        	.andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedCreate() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupCreatePatient();

        mockMvc.perform(post("/api/Patient").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testUserAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserAllowedIdWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/2345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"2345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedDelete() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupDeletePatient("12345");

        mockMvc.perform(delete("/api/Patient/12345").contentType("application/fhir+xml")
                .with(authentication(oauthTestAuthentication)))
                .andExpect(status().is2xxSuccessful());
    }
    
    @Test
    public void testUserAllowedConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedIdConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient/2345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"2345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserAllowedInstanceOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientInstanceOperation();

        mockMvc.perform(post("/api/Patient/12345/$instance")
        	.with(authentication(oauthTestAuthentication)))
        	.andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(post("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserAllowedIdByReference() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("2345");

        mockMvc.perform(get("/api/Observation?patient=Patient/2345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    // user/*.read scope tests
    @Test
    public void testUserReadAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }

    @Test
    public void testUserReadForbiddenWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUserReadForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    public void testUserReadForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(post("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // user/*.write scope tests
    @Test
    public void testUserWriteForbiddenRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testUserWriteAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testUserWriteAllowedConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserWriteForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/*.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    // user/Patient.* scope tests
    public void testUserResourceAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }

    @Test
    public void testUserResourceAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserResourceAllowedConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserResourceAllowedInstanceOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientInstanceOperation();

        mockMvc.perform(post("/api/Patient/12345/$instance")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserResourceAllowedTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserResourceForbiddenResource() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.*");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // user/Patient.read scope tests
    @Test
    public void testUserResourceReadAllowedRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }
    
    @Test
    public void testUserResourceReadAllowedIdRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("2345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|2345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isOk());
    }

    @Test
    public void testUserResourceReadForbiddenWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUserResourceReadForbiddenConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUserResourceReadForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
    
    @Test
    public void testUserResourceReadForbiddenResourceRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.read");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupGetLaboratoryObservations("12345");

        mockMvc.perform(get("/api/Observation?patient=Patient/12345")
        		.with(authentication(oauthTestAuthentication)))
                .andExpect(status().isForbidden());
    }

    // user/Patient.write scope tests
    @Test
    public void testUserResourceWriteForbiddenRead() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupSearchPatientByIdentifier("12345");

        mockMvc.perform(get("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }

    @Test
    public void testUserResourceWriteAllowedWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatient();

        mockMvc.perform(put("/api/Patient/12345").contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserResourceWriteAllowedConditionalWrite() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupUpdatePatientConditional("12345");

        mockMvc.perform(put("/api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|12345")
        		.contentType("application/fhir+xml")
                .content("<Patient><id value=\"12345\"/></Patient>").with(authentication(oauthTestAuthentication)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserResourceWriteForbiddenTypeOperation() throws Exception {
        Set<String> scopes = new HashSet<String>();
        scopes.add("user/Patient.write");
        Authentication oauthTestAuthentication = util.setupAuthentication(scopes, "12345", null);

        util.setupPatientTypeOperation();

        mockMvc.perform(get("/api/Patient/$type")
                .with(authentication(oauthTestAuthentication))).andExpect(status().isForbidden());
    }
}

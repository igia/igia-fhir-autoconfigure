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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;

@Component
public class ScopeBasedAuthorizationInterceptorTestUtil {
    @MockBean
    private TokenStore tokenStore;

    @MockBean
    private OAuth2ClientContext oauth2ClientContext;

    @MockBean
    private PrincipalExtractor principalExtractor;

    @MockBean
    private AuthoritiesExtractor authoritiesExtractor;

    @SpyBean
    private PatientResourceProvider patientService;

    @SpyBean
    private ObservationResourceProvider observationService;

    @Configuration
    @EnableOAuth2Client
    public static class LocalSecurityBeanOverrideConfiguration {
        @Bean
        public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
                OAuth2ProtectedResourceDetails details) {
            return new OAuth2RestTemplate(details, oauth2ClientContext);
        }
    }

    // GET /api/Patient/patientid
    protected void setupReadPatient(String patientid) {        
        Patient patient = new Patient();
        IIdType patientIId = new IdType("Patient", patientid);
        patient.setId(patientIId);        
        doReturn(patient).when(patientService).readPatient(any());
    }
    
    // GET /api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|patientid
    protected void setupSearchPatientByIdentifier(String patientid) {
        List<Patient> patients = new ArrayList<Patient>();
        Patient patient = new Patient();
        IIdType patientIId = new IdType("Patient", patientid);
        patient.setId(patientIId);
        patients.add(patient);
        doReturn(patients).when(patientService).searchPatientByIdentifier(any());
    }

    // POST /api/Patient with any body
    protected void setupCreatePatient() {
        MethodOutcome retVal = new MethodOutcome();
        doReturn(retVal).when(patientService).createPatient(any());
    }

    // PUT /api/Patient/patientid with any body
    protected void setupUpdatePatient() {
        MethodOutcome retVal = new MethodOutcome();
        doReturn(retVal).when(patientService).updatePatient(any(), any(), ArgumentMatchers.isNull());
    }

    // PUT
    // /api/Patient?identifier=http://igia.io/Patients/Identifier/MRN|patientid
    // with any body
    protected void setupUpdatePatientConditional(String patientid) {
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(new IdType("Patient", patientid, "1"));
        doReturn(retVal).when(patientService).updatePatient(any(), ArgumentMatchers.isNull(), any());
    }

    // DELETE /api/Patient/patientid
    protected void setupDeletePatient(String patientid) {
        doNothing().when(patientService).deletePatient(any());
    }

    // POST /api/Patient/$type with any body
    protected void setupPatientTypeOperation() {
        Bundle bundle = new Bundle();
        doReturn(bundle).when(patientService).patientTypeOperation();
    }

    // POST /api/Patient/patientid/$instance with any body
    protected void setupPatientInstanceOperation() {
        Bundle bundle = new Bundle();
        doReturn(bundle).when(patientService).patientInstanceOperation(any());
    }

    // GET /api/Observation?patient=Patient/patientid
    protected void setupGetLaboratoryObservations(String patientid) {
        List<Observation> observations = new ArrayList<Observation>();
        Observation observation = new Observation();
        IIdType id = new IdType("Observation", "testobservation");
        observation.setId(id);
        observation.setSubject(new Reference("Patient/" + patientid));
        observations.add(observation);
        when(observationService.searchLaboratoryObservations(any())).thenReturn(observations);
    }
    
    // PUT /api/Observation/observationid with any body
    protected void setupUpdateObservation() {
        MethodOutcome retVal = new MethodOutcome();
        doReturn(retVal).when(observationService).updateObservation(any(), any(), ArgumentMatchers.isNull());
    }

    // PUT
    // /api/Observation?identifier=http://any.org|observationid
    // with any body
    protected void setupUpdateObservationConditional(String observationid) {
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(new IdType("Observation", observationid, "1"));
        doReturn(retVal).when(observationService).updateObservation(any(), ArgumentMatchers.isNull(), any());
    }
    
    // POST /api/Observation/$type with any body
    protected void setupObservationTypeOperation() {
        Bundle bundle = new Bundle();
        doReturn(bundle).when(observationService).observationTypeOperation();
    }

    // POST /api/Observation/observationid/$instance with any body
    protected void setupObservationInstanceOperation() {
        Bundle bundle = new Bundle();
        doReturn(bundle).when(observationService).observationInstanceOperation(any());
    }

    protected Authentication setupAuthentication(Set<String> scopes, String patientid, String userid) {
        OAuth2Authentication oauth2Authentication = (OAuth2Authentication) getOauthTestAuthentication(scopes);

        CustomTokenConverter converter = new CustomTokenConverter(patientid, userid);
        converter.setSigningKey("123");
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setTokenEnhancer(converter);
        OAuth2AccessToken token = defaultTokenServices.createAccessToken(oauth2Authentication);

        when(oauth2ClientContext.getAccessToken()).thenReturn(token);
        when(tokenStore.readAccessToken(any())).thenReturn(token);
        when(principalExtractor.extractPrincipal(any())).thenReturn("");
        when(authoritiesExtractor.extractAuthorities(any())).thenReturn(new ArrayList<GrantedAuthority>());

        return oauth2Authentication;
    }

    private class CustomTokenConverter extends JwtAccessTokenConverter {
        private String patientid;
        private String userid;

        public CustomTokenConverter(String patientid, String userid) {
            this.patientid = patientid;
            this.userid = userid;
        }

        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {

            final Map<String, Object> additionalInfo = new HashMap<String, Object>();

            additionalInfo.put("patient", patientid);
            additionalInfo.put("user", userid);

            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
            accessToken = super.enhance(accessToken, authentication);
            return accessToken;
        }
    }

    private Authentication getOauthTestAuthentication(Set<String> scopes) {
        return new OAuth2Authentication(getOauth2Request(scopes), getAuthentication());
    }

    private OAuth2Request getOauth2Request(Set<String> scopes) {
        String clientId = "oauth-client-id";
        Map<String, String> requestParameters = Collections.emptyMap();
        boolean approved = true;
        String redirectUrl = "http://my-redirect-url.com";
        Set<String> responseTypes = Collections.emptySet();
        // Set<String> scopes = Collections.emptySet();
        Set<String> resourceIds = Collections.emptySet();
        Map<String, Serializable> extensionProperties = Collections.emptyMap();
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("Everything");

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, authorities, approved, scopes,
                resourceIds, redirectUrl, responseTypes, extensionProperties);

        return oAuth2Request;
    }

    private Authentication getAuthentication() {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("Everything");
        User userPrincipal = new User("user", "", true, true, true, true, authorities);
        HashMap<String, String> details = new HashMap<String, String>();

        TestingAuthenticationToken token = new TestingAuthenticationToken(userPrincipal, null, authorities);
        token.setAuthenticated(true);
        token.setDetails(details);

        return token;
    }

    @Component
    public static class PatientResourceProvider implements IResourceProvider {
        @Override
        public Class<? extends IBaseResource> getResourceType() {
            return Patient.class;
        }

        @Read
        public Patient readPatient(@IdParam IdType theId) {
            return null;
        }
        
        @Search
        public List<Patient> searchPatientByIdentifier(
                @RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam identifier) {
            return null;
        }

        @Create
        public MethodOutcome createPatient(@ResourceParam Patient thePatient) {
            return null;
        }

        @Update
        public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient, @ConditionalUrlParam String theConditional) {
            return null;
        }

        @Delete()
        public void deletePatient(@IdParam IdType theId) {
        }

        @Operation(name = "$type", idempotent = true)
        public Bundle patientTypeOperation() {
            return null;
        }

        @Operation(name = "$instance", idempotent = true)
        public Bundle patientInstanceOperation(@IdParam IdType theId) {
            return null;
        }
    }

    @Component
    public static class ObservationResourceProvider implements IResourceProvider {
        @Override
        public Class<? extends IBaseResource> getResourceType() {
            return Observation.class;
        }

        @Search
        public List<Observation> searchLaboratoryObservations(
                @RequiredParam(name = Observation.SP_PATIENT) ReferenceParam patient) {
            return null;
        }
        
        @Update
        public MethodOutcome updateObservation(@IdParam IdType theId, @ResourceParam Observation theObservation, @ConditionalUrlParam String theConditional) {
            return null;
        }
        
        @Operation(name = "$type", idempotent = true)
        public Bundle observationTypeOperation() {
            return null;
        }

        @Operation(name = "$instance", idempotent = true)
        public Bundle observationInstanceOperation(@IdParam IdType theId) {
            return null;
        }
    }
}
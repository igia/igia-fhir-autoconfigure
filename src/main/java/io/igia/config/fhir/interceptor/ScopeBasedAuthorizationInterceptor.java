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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ResourceFactory;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hspconsortium.platform.api.authorization.SmartScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleConditional;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleOp;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

public class ScopeBasedAuthorizationInterceptor extends AuthorizationInterceptor {
	public static final String LAUNCH_CONTEXT_PATIENT_PARAM_NAME = "patient";
	private static final String RULE_PATIENT_SCOPE_DEFAULT_DENY = "DENY ALL patient, resource or operation access if not explicitly granted in authorized scope";

	private TokenStore tokenStore;
	private final OAuth2RestTemplate oAuth2RestTemplate;
	
    @Autowired
    private PrincipalExtractor principalExtractor;

    @Autowired
    private AuthoritiesExtractor authoritiesExtractor;

	public ScopeBasedAuthorizationInterceptor(TokenStore tokenStore, OAuth2RestTemplate oAuth2RestTemplate) {
		this.tokenStore = tokenStore;
		this.oAuth2RestTemplate = oAuth2RestTemplate;
		// new flag in 3.5.0, allows attempts to perform read operations (read/search/history)
		// and then checks for matches by the interceptor after the method handler is called 
		this.setFlags(AuthorizationFlagsEnum.NO_NOT_PROACTIVELY_BLOCK_COMPARTMENT_READ_ACCESS);
	}

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// if the user is not authenticated, we can't do any authorization
		if (authentication == null || !(authentication instanceof OAuth2Authentication)) {
			return new RuleBuilder().allowAll().build();
		}
		
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(
				oAuth2RestTemplate.getOAuth2ClientContext().getAccessToken().getValue());
		
		Set<SmartScope> smartScopes = getSmartScopes(accessToken);
		
		IAuthRuleBuilder rules = new RuleBuilder();	

		// if no access limiting scopes, then allow all
		boolean isSmartScope = false;
		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isUserScope() || smartScope.isPatientScope())
				isSmartScope = true;
				break;				
		}
		if(!isSmartScope) {
			return new RuleBuilder().allowAll().build();
		}
		
		Map<String, Object> claims = accessToken.getAdditionalInformation();

		String patientId = (String) claims.get(LAUNCH_CONTEXT_PATIENT_PARAM_NAME);
		String userId = (String) principalExtractor.extractPrincipal(claims);
		List<GrantedAuthority> authorities = authoritiesExtractor.extractAuthorities(claims);
		
		rules = filterToUserScopes(rules, userId, authorities, smartScopes);
		rules = filterToPatientScopes(rules, patientId, smartScopes);
		
		rules.allow().metadata().andThen();
		rules.denyAll(RULE_PATIENT_SCOPE_DEFAULT_DENY).andThen();
		return rules.build();
	}
	
	protected IAuthRuleBuilder filterToUserScopes(IAuthRuleBuilder rules, String userId, List<GrantedAuthority> authorities, Set<SmartScope> smartScopes) {
		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isUserScope()) {
				filterToUserScope(userId, authorities, smartScope, rules);
			}
		}     
	
		return rules;
	}
	
	protected void filterToUserScope(String userId, List<GrantedAuthority> authorities, SmartScope smartScope, IAuthRuleBuilder rules) {		
		switch(smartScope.getOperation())
		{
		case "*":
			applyUserScopeResourceClassifier(rules.allow().read(), userId, authorities, smartScope);
			applyUserScopeResourceClassifier(rules.allow().write(), userId, authorities, smartScope);
			applyUserScopeResourceClassifier(rules.allow().delete(), userId, authorities, smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
			// instance and type level operations may read, alter or delete data, should restrict to "*" scope
			applyUserScopeOperationResourceClassifier(rules, userId, authorities, smartScope);
			break; 
		case "read":
			applyUserScopeResourceClassifier(rules.allow().read(), userId, authorities, smartScope);			
			break; 
		case "write":
			applyUserScopeResourceClassifier(rules.allow().write(), userId, authorities, smartScope);
			applyUserScopeResourceClassifier(rules.allow().delete(), userId, authorities, smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
			applyUserScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
			break; 
		default:
			throw new NotImplementedOperationException("Scope operation " + smartScope.getOperation() + " not supported.");
		}
	}
	
	protected void applyUserScopeResourceClassifier(IAuthRuleBuilderRuleOp ruleOp, String userId, List<GrantedAuthority> authorities,
			SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().withAnyId().andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).withAnyId().andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException(
					"Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}
	
	protected void applyUserScopeConditionalResourceClassifier(IAuthRuleBuilderRuleConditional ruleOp, SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException(
						"Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}
	
	protected void applyUserScopeOperationResourceClassifier(IAuthRuleBuilder rules, String userId, List<GrantedAuthority> authorities,
			SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			rules.allow().operation().withAnyName().atAnyLevel().andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				rules.allow().operation().withAnyName().onType(theType).andThen()
				.allow().operation().withAnyName().onInstancesOfType(theType).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException(
						"Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}

	protected IAuthRuleBuilder filterToPatientScopes(IAuthRuleBuilder rules, String patientId, Set<SmartScope> smartScopes) {
		IIdType patientIId = new IdType("Patient", patientId);				

		for (SmartScope smartScope : smartScopes) {
			if (smartScope.isPatientScope()) {
				if (patientId == null || patientId.isEmpty()) {
					throw new SecurityException("For patient scope, a launch context parameter indicating the in-context" +
							" patient is required, but none was found.");
				}

				filterToPatientScope(patientIId, smartScope, rules);
			}
		}     

		return rules;
	}

	protected void filterToPatientScope(IIdType patientId, SmartScope smartScope, IAuthRuleBuilder rules) {		
		switch(smartScope.getOperation())
		{
		case "*":
			applyPatientScopeResourceClassifier(rules.allow().read(), patientId, smartScope);
			applyPatientScopeResourceClassifier(rules.allow().write(), patientId, smartScope);
			applyPatientScopeResourceClassifier(rules.allow().delete(), patientId, smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
			// resource operations (type or instance level) may read, alter or delete data, should restrict to "*" scope
			applyPatientScopeOperationResourceClassifier(rules, patientId, smartScope);
			break; 
		case "read":
			applyPatientScopeResourceClassifier(rules.allow().read(), patientId, smartScope);			
			break; 
		case "write":
			applyPatientScopeResourceClassifier(rules.allow().write(), patientId, smartScope);
			applyPatientScopeResourceClassifier(rules.allow().delete(), patientId, smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().createConditional(), smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().updateConditional(), smartScope);
			applyPatientScopeConditionalResourceClassifier(rules.allow().deleteConditional(), smartScope);
			break; 
		default:
			throw new NotImplementedOperationException("Scope operation " + smartScope.getOperation() + " not supported.");
		}
	}

	protected void applyPatientScopeResourceClassifier(IAuthRuleBuilderRuleOp ruleOp, IIdType patientId,
			SmartScope smartScope) {
		if (smartScope.getResource().equalsIgnoreCase("*")) {
			ruleOp.allResources().inCompartment("Patient", patientId).andThen();
		} else {
			Class<? extends IBaseResource> theType;
			try {
				theType = ResourceFactory.createResource(smartScope.getResource()).getClass();
				ruleOp.resourcesOfType(theType).inCompartment("Patient", patientId).andThen();
			} catch (FHIRException e) {
				throw new NotImplementedOperationException(
						"Scope resource " + smartScope.getResource() + " not supported.");
			}
		}
	}
	
	protected void applyPatientScopeConditionalResourceClassifier(IAuthRuleBuilderRuleConditional ruleOp, SmartScope smartScope) {
		return;
	}

	protected void applyPatientScopeOperationResourceClassifier(IAuthRuleBuilder rules, IIdType patientIdType,
			SmartScope smartScope) {
		//cannot fully restrict access to type and instance level operations by patient id
		if (smartScope.getResource().equalsIgnoreCase("Patient")) {
			rules.allow().operation().withAnyName().onInstance(patientIdType).andThen();
		}
	}

	private Set<SmartScope> getSmartScopes(OAuth2AccessToken token) {
		Set<SmartScope> scopes = new HashSet<>();

		for (String scope : token.getScope()) {
			scopes.add(new IgiaSmartScope(scope));
		}

		return scopes;
	}
}

Spring boot autoconfiguration library designed to give HAPI FHIR servlet capability to a JHipster generated microservice. Includes additional components to support SMART-on-FHIR launch.

# HAPI FHIR server Spring boot autoconfiguration

 This library is based on the [hapi-fhir-spring-boot-autoconfigure](https://github.com/jamesagnew/hapi-fhir/tree/master/hapi-fhir-spring-boot) project. Any Spring components in your microservice that implement HAPI IResourceProvider, IServerInterceptor, or IPagingProvider will be registered as providers with the HAPI FHIR server. At least one component implementing IResourceProvider must be available in order for the HAPI FHIR RestController to be created.

The server will be configured with default HAPI interceptors including a ResponseHighlighterInterceptor, LoggingInterceptor, ExceptionHandlingInterceptor, and a CorsInterceptor, as well as a DefaultThymeleafNarrativeGenerator. The SMART components described below are included if the server configuration property hspc.platform.api.security.mode is set to 'secured' or 'mock'.

Any of these components can be customized by providing your own beans that override those in the igia-fhir-auto-configuration class. In addition, custom server configuration can be added by implementing the FhirRestfulServerCustomizer interface or extending the igiarestfulServerCustomizer class.

# Capability statement enhancement

The autoconfiguration library includes a Conformance provider component that uses the HSPC hspc-reference-api-smart-support library to add authorize, token, and register endpoint URLs to capability statement, as described in the [SMART official documentation](http://www.hl7.org/fhir/smart-app-launch/capability-statement/). Implementation also adds a new property hspc.platform.authorization.smart.capabilities to include the `http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities` extensions that describe the server's SMART conformance.

# Scope-based authorization interceptor

A default implementation of a HAPI FHIR AuthorizationInterceptor to apply SMART scopes to API requests. The interceptor allows read/search/history operations to run then intercepts results to apply allowed scopes. If returned records are not permitted based on the client's scopes, it will return an unauthorized error to the client. See the SMART-on-FHIR documentation for details describing the level of scope enforcement.

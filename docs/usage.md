# Configuration

## HAPI FHIR autoconfiguration

Spring boot properties to configure the HAPI FHIR servlet.

```
hapi:
  fhir:
    version: dstu3 # dstu3 is the only supported version currently
    server:
      path: /api # base path on your server for HAPI FHIR servlet
    rest: # configuration properties for HAPI RestfulServer class
      server-name: igia-fhir-api-example
      server-version: 0.3.3
      implementation-description: igia Example FHIR Server
      default-response-encoding: json
      e-tag-support: enabled
      default-pretty-print: true
      exception.stacktrace: true
```

## SMART support configuration

Spring boot properties to configure the SMART framework support.

The microservice needs to be configured with a security.oauth2.resource.jwk.key-set-uri property with value pointing to the auth provider's JWKS certs endpoint.

Note that for the platform Keycloak SMART implementation, the hspc.platform.authorizartion.tokenUrlPath does not use the openid-connect protocol, but instead the smart-openid-connect protocol. This custom endpoint implements support for SMART required launch claims in the access token response.

```
security:
  oauth2:
    resource:
      jwk:
        key-set-uri: http://localhost:9080/auth/realms/igia/protocol/openid-connect/certs # required for scope enforcement

hspc:
  platform:
    api:
      security:
        # determines if security is applied at the contextPath, options are secured, mock or open
        mode: secured
      oauth2: # these values are placeholders required by HSPC library
        clientId:
        clientSecret:
        scopes:
    authorization:
      protocol: http #https
      host: ${AUTH_HOST:localhost}
      port: ${AUTH_PORT:8080}
      context: ~
      url: ${hspc.platform.authorization.protocol}://${hspc.platform.authorization.host}:${hspc.platform.authorization.port}${hspc.platform.authorization.context}
      authorizeUrlPath: /auth/realms/igia/protocol/openid-connect/auth
      tokenUrlPath: /auth/realms/igia/protocol/smart-openid-connect/token
      tokenCheckUrlPath: /auth/realms/igia/protocol/openid-connect/token/introspect
      userinfoUrlPath: /auth/realms/igia/protocol/openid-connect/userinfo
      smart:
        launchUrlPath: #placeholder required by library but not implemented in auth server
        registrationEndpointUrlPath: /auth/realms/igia/clients-registrations/openid-connect
        urisEndpointExtensionUrl: http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris
        launchRegistrationUrl: #placeholder required by library but not implemented in auth server
        # comma separate list of supported SMART capabilities for extension http://fhir-registry.smarthealthit.org/StructureDefinition/capabilities
        capabilities: launch-standalone, client-public, client-confidential-symmetric, sso-openid-connect, context-standalone-patient, permission-patient, permission-offline
    # the manifest output should contain these values (used to allow the API to publish different auth URL than it resolves locally, ie, Docker container)
    manifest:
      override: ${MANIFEST_OVERRIDE:false}
      protocol: http
      host: ${PUBLISHED_AUTH_HOST:}
      port: ${PUBLISHED_AUTH_PORT:}
      context: ~
      url: ${hspc.platform.manifest.protocol}://${hspc.platform.manifest.host}:${hspc.platform.manifest.port}${hspc.platform.manifest.context}
```

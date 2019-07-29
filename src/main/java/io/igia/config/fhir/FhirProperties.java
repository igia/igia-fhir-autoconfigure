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

/*-
 * Based on FhirProperties
 * hapi-fhir-spring-boot-autoconfigure
 * 
 */


import ca.uhn.fhir.context.FhirVersionEnum;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hapi.fhir")
public class FhirProperties {

    private FhirVersionEnum version = FhirVersionEnum.DSTU2;

    private Server server = new Server();

    private Validation validation = new Validation();

    public FhirVersionEnum getVersion() {
        return version;
    }

    public void setVersion(FhirVersionEnum version) {
        this.version = version;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public static class Server {

        private String url;

        private String path = "/fhir/*";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Validation {

        private boolean enabled = true;

        private boolean requestOnly = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequestOnly() {
            return requestOnly;
        }

        public void setRequestOnly(boolean requestOnly) {
            this.requestOnly = requestOnly;
        }
    }
}


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

import org.hspconsortium.platform.api.authorization.SmartScope;

public class IgiaSmartScope extends SmartScope{
	private String scope;

	public IgiaSmartScope(String scope) {
		super(scope);
		this.scope = scope;
	}
	
	@Override
    public String getResource(){
		//fix logic in base class, and not or
        if(!isPatientScope() && !isUserScope())
            return null;

        int forwardSlashIndex = this.scope.indexOf("/");
        int periodIndex = this.scope.indexOf(".");

        return this.scope.substring(forwardSlashIndex + 1, periodIndex);
    }

    @Override
    public String getOperation(){
    		//fix logic in base class, and not or
        if(!isPatientScope() && !isUserScope())
            return null;

        int periodIndex = this.scope.indexOf(".");

        return this.scope.substring(periodIndex + 1);
    }
}

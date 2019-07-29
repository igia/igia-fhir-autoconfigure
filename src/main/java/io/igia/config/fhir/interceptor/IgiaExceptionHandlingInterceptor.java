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

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class IgiaExceptionHandlingInterceptor extends ExceptionHandlingInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(IgiaExceptionHandlingInterceptor.class);

    @Override
    public boolean handleException(RequestDetails theRequestDetails,
                                   BaseServerResponseException theException,
                                   HttpServletRequest theServletRequest,
                                   HttpServletResponse theServletResponse)
        throws ServletException, IOException {
        return true;
    }

    /* 
     * Add UUID to operation outcome for exception tracking
     */
    @Override
    public BaseServerResponseException preProcessOutgoingException(RequestDetails theRequestDetails,
                                                                   Throwable theException,
                                                                   HttpServletRequest theServletRequest) throws ServletException {

        String uUID = UUID.randomUUID().toString();
        logger.warn("Logging exception OperationOutcome Id: {}", uUID);

        BaseServerResponseException retVal = super.preProcessOutgoingException(
        		theRequestDetails,
        		theException,
        		theServletRequest);
        retVal.getOperationOutcome().setId(uUID);
        return retVal;
    }

    @Override
    public ExceptionHandlingInterceptor setReturnStackTracesForExceptionTypes(Class<?>... theExceptionTypes) {
        super.setReturnStackTracesForExceptionTypes(theExceptionTypes);
        return this;
    }
}
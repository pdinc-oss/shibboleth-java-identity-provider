/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.authn.impl;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.webflow.Event;
import net.shibboleth.ext.spring.webflow.Events;
import net.shibboleth.idp.authn.AbstractAuthenticationAction;
import net.shibboleth.idp.authn.AuthenticationException;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UserAgentContext;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.net.InetAddresses;

/**
 * A stage that extracts the user-agent's IP address from the incoming requests, creates an
 * {@link UserAgentAddressContext}, and attaches it to the {@link AuthenticationContext}.
 */
@Events({@Event(id = EventIds.PROCEED_EVENT_ID),
        @Event(id = AuthnEventIds.NO_CREDENTIALS, description = "request does not contain user agent's IP address")})
public class ExtractUserAgentAddress extends AbstractAuthenticationAction {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ExtractUserAgentAddress.class);

    /** {@inheritDoc} */
    protected org.springframework.webflow.execution.Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) throws AuthenticationException {

        final String addressString = Constraint.isNotNull(profileRequestContext.getHttpRequest(),
                "HttpServletRequest cannot be null").getRemoteAddr();
        if (!InetAddresses.isInetAddress(addressString)) {
            log.debug("Action {}: User agent's IP address, {}, is not a valid IP address", getId(), addressString);
            return ActionSupport.buildEvent(this, AuthnEventIds.NO_CREDENTIALS);
        }

        authenticationContext.getSubcontext(UserAgentContext.class, true).setAddress(
                InetAddresses.forString(addressString));

        return ActionSupport.buildProceedEvent(this);
    }
}
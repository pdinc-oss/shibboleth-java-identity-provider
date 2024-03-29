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

package net.shibboleth.idp.saml.profile.config.logic;

import javax.annotation.Nullable;

import net.shibboleth.idp.profile.config.logic.ResolveAttributesPredicate;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;

import org.opensaml.profile.context.ProfileRequestContext;

/**
 * A predicate that evaluates a {@link ProfileRequestContext} and determines whether attribute resolution
 * and filtering should take place.
 * 
 * <p>For SAML 1 and SAML 2 SSO profiles, the "resolveAttributes" flag is the setting governing
 * this decision. For other profiles, false is returned.</p>
 * 
 * @deprecated
 */
@Deprecated(since="4.2.0", forRemoval=true)
public class ResolveAttributesProfileConfigPredicate extends ResolveAttributesPredicate {

    /** {@inheritDoc} */
    public boolean test(@Nullable final ProfileRequestContext input) {
        DeprecationSupport.warnOnce(ObjectType.CLASS, getClass().getName(), null, ResolveAttributesPredicate.class.getName());
        
        return super.test(input);
    }

}
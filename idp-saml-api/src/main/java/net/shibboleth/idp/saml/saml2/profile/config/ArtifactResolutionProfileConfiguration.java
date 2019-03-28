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

package net.shibboleth.idp.saml.saml2.profile.config;

import javax.annotation.Nonnull;

import org.opensaml.profile.logic.NoConfidentialityMessageChannelPredicate;
import org.opensaml.profile.logic.NoIntegrityMessageChannelPredicate;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/** Configuration support for SAML 2 artifact resolution requests. */
public class ArtifactResolutionProfileConfiguration extends AbstractSAML2ProfileConfiguration {

    /** ID for this profile configuration. */
    @Nonnull @NotEmpty public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/saml2/query/artifact";

    /** Constructor. */
    public ArtifactResolutionProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected ArtifactResolutionProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSignResponsesPredicate(new NoIntegrityMessageChannelPredicate());
        setEncryptAssertionsPredicate(new NoConfidentialityMessageChannelPredicate());
    }
    
}
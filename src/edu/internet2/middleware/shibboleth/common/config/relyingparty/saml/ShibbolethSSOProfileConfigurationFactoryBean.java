/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.common.config.relyingparty.saml;

import edu.internet2.middleware.shibboleth.common.relyingparty.saml1.ShibbolethSSOConfiguration;

/**
 * Spring factory for Shibboleth SSO profile configurations.
 */
public class ShibbolethSSOProfileConfigurationFactoryBean extends AbstractSAMLProfileConfigurationFactoryBean {

    /** {@inheritDoc} */
    public Class getObjectType() {
        return ShibbolethSSOConfiguration.class;
    }

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        ShibbolethSSOConfiguration configuration = new ShibbolethSSOConfiguration();
        configuration.setAssertionAudiences(getAudiences());
        configuration.setAssertionLifetime(getAssertionLifetime());
        configuration.setDefaultArtifactType(getDefaultArtifactType());
        configuration.setDefaultNameIDFormat(getDefaultNameFormat());
        configuration.setSignAssertions(isSignAssertions());
        configuration.setSigningCredential(getSigningCredential());

        return configuration;
    }
}
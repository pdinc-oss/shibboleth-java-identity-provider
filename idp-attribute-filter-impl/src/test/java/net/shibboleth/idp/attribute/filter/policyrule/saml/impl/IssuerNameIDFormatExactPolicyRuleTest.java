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

package net.shibboleth.idp.attribute.filter.policyrule.saml.impl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * test for {@link IssuerNameIDFormatExactPolicyRule}.
 */
@SuppressWarnings("javadoc")
public class IssuerNameIDFormatExactPolicyRuleTest extends BaseMetadataTests {

    private IssuerNameIDFormatExactPolicyRule getMatcher(String format) throws ComponentInitializationException {
        IssuerNameIDFormatExactPolicyRule matcher = new IssuerNameIDFormatExactPolicyRule();
        matcher.setId("matcher");
        matcher.setNameIdFormat(format);
        matcher.initialize();
        return matcher;
    }


    @Test public void simple() throws ComponentInitializationException {
        IssuerNameIDFormatExactPolicyRule matcher = getMatcher("https://example.org/foo");

        assertEquals(matcher.getNameIdFormat(), "https://example.org/foo");

        assertEquals(matcher.matches(issMetadataContext(noneEntity, "Principal")), Tristate.FALSE);
        assertEquals(matcher.matches(issMetadataContext(idpEntity, "Principal")), Tristate.TRUE);
        assertEquals(matcher.matches(issMetadataContext(jiraEntity, "Principal")), Tristate.FALSE);
    }

}
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

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import net.shibboleth.idp.attribute.filter.PolicyRequirementRule.Tristate;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * test for {@link IssuerEntityAttributeRegexPolicyRule}.
 */
@SuppressWarnings("javadoc")
public class IssuerEntityAttributeRegexPolicyRuleTest  extends BaseMetadataTests {

    private IssuerEntityAttributeRegexPolicyRule getMatcher() throws ComponentInitializationException {
        Pattern pattern = Pattern.compile("urn\\:example.org\\:policy\\:56.*");
        return getMatcher("urn:example.org:policies", pattern, null);
    }

    private IssuerEntityAttributeRegexPolicyRule getMatcher(String attributeName, Pattern attributeValuePattern,
            String attributeNameFormat) throws ComponentInitializationException {
        IssuerEntityAttributeRegexPolicyRule matcher = new IssuerEntityAttributeRegexPolicyRule();
        matcher.setId("matcher");
        matcher.setAttributeName(attributeName);
        matcher.setValueRegex(attributeValuePattern);
        matcher.setNameFormat(attributeNameFormat);
        matcher.initialize();
        return matcher;
    }

    @Test public void simple() throws ComponentInitializationException {

        IssuerEntityAttributeRegexPolicyRule matcher = getMatcher();
        assertEquals(matcher.matches(issMetadataContext(idpEntity, "Principal")), Tristate.TRUE);

        assertEquals(matcher.matches(issMetadataContext(jiraEntity, "Principal")), Tristate.FALSE);

    }

    @Test public void getter() throws ComponentInitializationException {
        assertEquals(getMatcher().getValueRegex().pattern(), "urn\\:example.org\\:policy\\:56.*");
    }
}

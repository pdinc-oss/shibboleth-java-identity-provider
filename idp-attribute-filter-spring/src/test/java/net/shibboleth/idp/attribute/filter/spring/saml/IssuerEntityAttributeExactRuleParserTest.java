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

package net.shibboleth.idp.attribute.filter.spring.saml;

import static org.testng.Assert.*;

import org.springframework.beans.factory.BeanCreationException;
import org.testng.annotations.Test;

import net.shibboleth.idp.attribute.filter.policyrule.saml.impl.IssuerEntityAttributeExactPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.testing.BaseAttributeFilterParserTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * test for {@link IssuerEntityAttributeExactPolicyRule}.
 */
@SuppressWarnings("javadoc")
public class IssuerEntityAttributeExactRuleParserTest extends BaseAttributeFilterParserTest {

    @Test public void basic() throws ComponentInitializationException {
        IssuerEntityAttributeExactPolicyRule rule =
                (IssuerEntityAttributeExactPolicyRule) getPolicyRule("issuerEA2.xml");

        assertEquals(rule.getValue(), "urn:example.org:policy:ABCD1234");
        assertEquals(rule.getAttributeName(), "urn:example.org:policy");
        assertTrue(rule.getIgnoreUnmappedEntityAttributes());
    }
    
    @Test(expectedExceptions = {BeanCreationException.class}) public void empty() throws ComponentInitializationException {
        IssuerEntityAttributeExactPolicyRule rule =
                (IssuerEntityAttributeExactPolicyRule) getPolicyRule("issuerEA3.xml");

        assertEquals(rule.getValue(), "urn:example.org:policy:ABCD1234");
        assertEquals(rule.getAttributeName(), "urn:example.org:policy");
        assertFalse(rule.getIgnoreUnmappedEntityAttributes());
    }

}

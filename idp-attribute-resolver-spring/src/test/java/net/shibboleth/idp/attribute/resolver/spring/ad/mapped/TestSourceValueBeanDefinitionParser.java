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

package net.shibboleth.idp.attribute.resolver.spring.ad.mapped;

import net.shibboleth.idp.attribute.resolver.impl.ad.mapped.SourceValue;
import net.shibboleth.idp.attribute.resolver.spring.ad.BaseTestAttributeDefinitionBeanParser;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link SourceValueBeanDefinitionParser}.
 */
public class TestSourceValueBeanDefinitionParser extends BaseTestAttributeDefinitionBeanParser {

    private SourceValue getSourceValue(String fileName) {

        GenericApplicationContext context = new GenericApplicationContext();
        context.setDisplayName("ApplicationContext: " + TestSourceValueBeanDefinitionParser.class);

        return getBean("mapped/" + fileName, SourceValue.class, context);
    }

    @Test public void testSimple() {
        SourceValue value = getSourceValue("sourceValue.xml");
        
        Assert.assertFalse(value.isIgnoreCase());
        Assert.assertFalse(value.isPartialMatch());
        Assert.assertNull(value.getValue());
    }
    
    @Test public void testValues1() {
        SourceValue value = getSourceValue("sourceValueAttributes1.xml");
        
        Assert.assertTrue(value.isIgnoreCase());
        Assert.assertTrue(value.isPartialMatch());
        Assert.assertEquals(value.getValue(), "sourceValueAttributes1");
    }

    @Test public void testValues2() {
        SourceValue value = getSourceValue("sourceValueAttributes2.xml");
        
        Assert.assertFalse(value.isIgnoreCase());
        Assert.assertFalse(value.isPartialMatch());
        Assert.assertEquals(value.getValue(), "sourceValueAttributes2");
    }
}

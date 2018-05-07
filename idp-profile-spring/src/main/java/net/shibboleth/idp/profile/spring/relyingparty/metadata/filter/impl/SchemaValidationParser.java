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

package net.shibboleth.idp.profile.spring.relyingparty.metadata.filter.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.opensaml.saml.metadata.resolver.filter.impl.SchemaValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.metadata.AbstractMetadataProviderParser;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

/**
 * Parser for a &lt;SchemaValidation&gt; filter.
 */
public class SchemaValidationParser extends AbstractSingleBeanDefinitionParser {

    /** Element name. */
    @Nonnull public static final QName TYPE_NAME =
            new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "SchemaValidation");

    /** Element name for the extension Schema. */
    @Nonnull public static final QName EXTENSION_SCHEMA_NAME =
            new QName(AbstractMetadataProviderParser.METADATA_NAMESPACE, "ExtensionSchema");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SchemaValidationParser.class);

    /** {@inheritDoc} */
    @Override protected Class<?> getBeanClass(final Element element) {
        return SchemaValidationFilter.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(final Element element, final ParserContext parserContext,
            final BeanDefinitionBuilder builder) {
        final List<Element> schemaNameElements = ElementSupport.getChildElements(element, EXTENSION_SCHEMA_NAME);

        if (element.hasAttributeNS(null, "schemaBuilderRef")) {
            builder.addConstructorArgReference(StringSupport.trimOrNull(element
                    .getAttributeNS(null, "schemaBuilderRef")));
        } else {
            builder.addConstructorArgReference("shibboleth.SchemaBuilder");
        }
        if (null != schemaNameElements && !schemaNameElements.isEmpty()) {
            DeprecationSupport.warn(ObjectType.ELEMENT, "ExtensionSchema", null, null);
            builder.addConstructorArgValue(SpringSupport.getElementTextContentAsManagedList(schemaNameElements));
        }
    }

    /** {@inheritDoc} */
    @Override protected boolean shouldGenerateId() {
        return true;
    }
    
}
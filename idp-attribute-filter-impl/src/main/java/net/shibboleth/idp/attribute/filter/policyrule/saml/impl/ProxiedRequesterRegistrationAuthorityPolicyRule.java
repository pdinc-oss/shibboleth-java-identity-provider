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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.filter.context.AttributeFilterContext;

import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * Rule based on RPI extension in proxied requester's metadata.
 */
public class ProxiedRequesterRegistrationAuthorityPolicyRule extends AbstractRegistrationAuthorityPolicyRule {

    /** {@inheritDoc} */
    @Override
    @Nullable protected EntityDescriptor getEntityMetadata(@Nonnull final AttributeFilterContext filterContext) {
        final SAMLMetadataContext metadataContext = filterContext.getProxiedRequesterMetadataContext();
        return metadataContext != null ? metadataContext.getEntityDescriptor() : null;
    }

}
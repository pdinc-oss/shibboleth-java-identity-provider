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

package net.shibboleth.idp.saml.attribute.mapping.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.saml.attribute.mapping.AttributesMapper;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.service.ReloadableService;
import net.shibboleth.utilities.java.support.service.ServiceableComponent;

import org.joda.time.DateTime;
import org.opensaml.saml.saml1.core.AttributeDesignator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * An implementation of {@link SAML1AttributeDesignatorsMapper} for use by objects that
 * can't just store a constructed mapper themselves, such as action beans.
 */
@ThreadSafe
public class SAML1AttributeDesignatorsMapperService implements AttributesMapper<AttributeDesignator,IdPAttribute> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML1AttributeDesignatorsMapperService.class);

    /** Service used to get the resolver used to fetch attributes. */
    @Nonnull private final ReloadableService<AttributeResolver> attributeResolverService;

    /** Whether the last invocation of {@link #refreshMappers()} failed. */
    @Nonnull private boolean refreshFailed;

    /** Cached AttributeMapper. */
    @Nullable private SAML1AttributeDesignatorsMapper attributesMapper;

    /** Date when the cache was last refreshed. */
    @Nullable private DateTime lastReload;

    /**
     * Constructor.
     * 
     * @param resolverService the service for the attribute resolver we are to derive mapping info from
     */
    public SAML1AttributeDesignatorsMapperService(@Nonnull final ReloadableService<AttributeResolver> resolverService) {
        attributeResolverService = Constraint.isNotNull(resolverService, "AttributeResolver cannot be null");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NonnullElements public Multimap<String, IdPAttribute> mapAttributes(
            @Nonnull @NonnullElements final List<AttributeDesignator> prototypes) {
        
        final SAML1AttributeDesignatorsMapper mapper = getMapper();
        if (null == mapper) {
            log.error("No AttributeMapper available, returning nothing");
            return ArrayListMultimap.create();
        }
        
        return mapper.mapAttributes(prototypes);
    }

    /**
     * Check to see if a reload of the mapper is required and do so, and return whatever should be used.
     * 
     * @return the mapper to use
     */
    @Nullable private SAML1AttributeDesignatorsMapper getMapper() {
        
        if (lastReload != null && lastReload.equals(attributeResolverService.getLastSuccessfulReloadInstant())) {
            // Nothing has changed since we last reloaded.
            return attributesMapper;
        }

        // Reload.
        ServiceableComponent<AttributeResolver> component = null;
        SAML1AttributeDesignatorsMapper am = null;
        try {
            // Get date before we get the component. That way we'll not leak changes.
            final DateTime when = attributeResolverService.getLastSuccessfulReloadInstant();
            component = attributeResolverService.getServiceableComponent();
            if (null == component) {
                if (!refreshFailed) {
                    log.error("Invalid AttributeResolver configuration");
                }
                refreshFailed = true;
            } else {
                final AttributeResolver attributeResolver = component.getComponent();
                am = new SAML1AttributeDesignatorsMapper(attributeResolver);
                refreshFailed = false;
                lastReload = when;
            }
        } finally {
            if (null != component) {
                component.unpinComponent();
            }
        }
        
        try {
            if (null != am) {
                am.initialize();
            }
        } catch (final ComponentInitializationException e) {
            log.error("Error initializing AttributeMapper", e);
        }
        
        attributesMapper = am;
        return am;
    }
    
}
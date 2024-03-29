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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.EmptyAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.PluginDependencySupport;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An {@link AttributeDefinition} that creates an attribute whose values are the
 * values of all its dependencies.
 */
@ThreadSafe
public class SimpleAttributeDefinition extends AbstractAttributeDefinition {

    /** Do we strip {@link EmptyAttributeValue}s? */
    private boolean stripNulls;

    /** Set our "Null" strategy.
     * @param what The value to set.
     */
    public void setStripNulls(final boolean what) {
        stripNulls = what;
    }

   /** Do we strip nulls?
     * @return Returns whether we strip Nulls.
     */
    public boolean isStripNulls() {
        return stripNulls;
    }

    /** {@inheritDoc} */
    @Override @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        final IdPAttribute result = new IdPAttribute(getId());
        final List<IdPAttributeValue> values = PluginDependencySupport.getMergedAttributeValues(workContext,
                getAttributeDependencies(), 
                getDataConnectorDependencies(), 
                getId());
        if (isStripNulls()) {
            result.setValues(
                    values.stream().
                    filter(e -> e != null && !(e instanceof EmptyAttributeValue)).
                    collect(Collectors.toUnmodifiableList()));
        } else {
            result.setValues(values);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (getDataConnectorDependencies().isEmpty() && getAttributeDependencies().isEmpty()) {
            throw new ComponentInitializationException(getLogPrefix() + " no dependencies were configured");
        }
    }
  
}
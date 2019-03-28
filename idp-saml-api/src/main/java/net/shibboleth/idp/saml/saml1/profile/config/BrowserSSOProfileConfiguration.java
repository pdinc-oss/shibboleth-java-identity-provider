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

package net.shibboleth.idp.saml.saml1.profile.config;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import net.shibboleth.idp.authn.config.AuthenticationProfileConfiguration;
import net.shibboleth.idp.saml.authn.principal.AuthenticationMethodPrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** Configuration for SAML 1 Browser SSO profile requests. */
public class BrowserSSOProfileConfiguration extends AbstractSAML1ArtifactAwareProfileConfiguration
        implements AuthenticationProfileConfiguration {

    /** ID for this profile configuration. */
    @Nonnull @NotEmpty public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/saml1/sso/browser";

    /** Whether attributes should be resolved in the course of the profile. */
    @Nonnull private Predicate<ProfileRequestContext> resolveAttributesPredicate;

    /** Whether responses to the authentication request should include an attribute statement. */
    @Nonnull private Predicate<ProfileRequestContext> includeAttributeStatementPredicate;
    
    /** Whether to mandate forced authentication for the request. */
    @Nonnull private Predicate<ProfileRequestContext> forceAuthnPredicate;

    /** Lookup function to supply default authentication methods. */
    @Nonnull private Function<ProfileRequestContext,Collection<AuthenticationMethodPrincipal>>
            defaultAuthenticationMethodsLookupStrategy;

    /** Lookup function to supply authentication flows. */
    @Nonnull private Function<ProfileRequestContext,Set<String>> authenticationFlowsLookupStrategy;

    /** Lookup function to supply post authentication flows. */
    @Nonnull private Function<ProfileRequestContext,Collection<String>> postAuthenticationFlowsLookupStrategy;

    /** Lookup function to supply NameIdentifier formats. */
    @Nonnull private Function<ProfileRequestContext,Collection<String>> nameIDFormatPrecedenceLookupStrategy;
    
    /** Constructor. */
    public BrowserSSOProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected BrowserSSOProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSignResponses(true);
        resolveAttributesPredicate = Predicates.alwaysTrue();
        includeAttributeStatementPredicate = Predicates.alwaysFalse();
        authenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        postAuthenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        defaultAuthenticationMethodsLookupStrategy = FunctionSupport.constant(null);
        nameIDFormatPrecedenceLookupStrategy = FunctionSupport.constant(null);
        forceAuthnPredicate = Predicates.alwaysFalse();
    }

    /**
     * Get whether attributes should be resolved during the profile.
     *
     * <p>Default is true</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return true iff attributes should be resolved
     */
    public boolean isResolveAttributes(@Nullable final ProfileRequestContext profileRequestContext) {
        return resolveAttributesPredicate.test(profileRequestContext);
    }

    /**
     * Set whether attributes should be resolved during the profile.
     * 
     * @param flag flag to set
     */
    public void setResolveAttributes(final boolean flag) {
        resolveAttributesPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether attributes should be resolved during the profile.
     * 
     * @param condition condition to set
     */
    public void setResolveAttributesPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        resolveAttributesPredicate = Constraint.isNotNull(condition, "Resolve attributes predicate cannot be null");
    }

    /**
     * Get whether responses to the authentication request should include an attribute statement.
     *
     * <p>Default is true</p>
     * 
     * @param profileRequestContext current profile request context
     *
     * @return whether responses to the authentication request should include an attribute statement
     */
    public boolean isIncludeAttributeStatement(@Nullable final ProfileRequestContext profileRequestContext) {
        return includeAttributeStatementPredicate.test(profileRequestContext);
    }

    /**
     * Set whether responses to the authentication request should include an attribute statement.
     *
     * @param flag flag to set
     */
    public void setIncludeAttributeStatement(final boolean flag) {
        includeAttributeStatementPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether responses to the authentication request should include an
     * attribute statement.
     *
     * @param condition  condition to set
     */
    public void setIncludeAttributeStatementPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        includeAttributeStatementPredicate = Constraint.isNotNull(condition,
                "Include attribute statement predicate cannot be null");
    }
    
    /** {@inheritDoc} */
    public boolean isForceAuthn(@Nullable final ProfileRequestContext profileRequestContext) {
        return forceAuthnPredicate.test(profileRequestContext);
    }
    
    /**
     * Set whether a fresh user presence proof should be required for this request.
     * 
     * @param flag flag to set
     */
    public void setForceAuthn(final boolean flag) {
        forceAuthnPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether a fresh user presence proof should be required for this request.
     * 
     * @param condition condition to set
     */
    public void setForceAuthnPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        forceAuthnPredicate = Constraint.isNotNull(condition, "Forced authentication predicate cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<Principal> getDefaultAuthenticationMethods(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return CollectionSupport.buildImmutableList(
                defaultAuthenticationMethodsLookupStrategy.apply(profileRequestContext));
    }
    
    /**
     * Set the default authentication methods to use, expressed as custom principals.
     * 
     * @param methods   default authentication methods to use
     */
    public void setDefaultAuthenticationMethods(
            @Nullable @NonnullElements final Collection<AuthenticationMethodPrincipal> methods) {

        if (methods != null) {
            defaultAuthenticationMethodsLookupStrategy =
                    FunctionSupport.constant(new ArrayList<>(Collections2.filter(methods, Predicates.notNull())));
        } else {
            defaultAuthenticationMethodsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the {@link #defaultAuthenticationMethods} property.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setDefaultAuthenticationMethodsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<AuthenticationMethodPrincipal>> strategy) {
        defaultAuthenticationMethodsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Set<String> getAuthenticationFlows(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return CollectionSupport.buildImmutableSet(authenticationFlowsLookupStrategy.apply(profileRequestContext));
    }

    /**
     * Set the authentication flows to use.
     * 
     * @param flows   flow identifiers to use
     */
    public void setAuthenticationFlows(@Nullable @NonnullElements final Collection<String> flows) {
        if (flows != null) {
            authenticationFlowsLookupStrategy =
                    FunctionSupport.constant(new HashSet<>(StringSupport.normalizeStringCollection(flows)));
        } else {
            authenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the authentication flows to use.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setAuthenticationFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Set<String>> strategy) {
        authenticationFlowsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getPostAuthenticationFlows(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return CollectionSupport.buildImmutableList(postAuthenticationFlowsLookupStrategy.apply(profileRequestContext));
    }

    /**
     * Set the ordered collection of post-authentication interceptor flows to enable.
     * 
     * @param flows   flow identifiers to enable
     */
    public void setPostAuthenticationFlows(@Nullable @NonnullElements final Collection<String> flows) {
        if (flows != null) {
            postAuthenticationFlowsLookupStrategy =
                    FunctionSupport.constant(new ArrayList<>(StringSupport.normalizeStringCollection(flows)));
        } else {
            postAuthenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the post-authentication interceptor flows to enable.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setPostAuthenticationFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        postAuthenticationFlowsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getNameIDFormatPrecedence(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return CollectionSupport.buildImmutableList(nameIDFormatPrecedenceLookupStrategy.apply(profileRequestContext));
    }

    /**
     * Set the name identifier formats to use.
     * 
     * @param formats   name identifier formats to use
     */
    public void setNameIDFormatPrecedence(@Nonnull @NonnullElements final Collection<String> formats) {
        Constraint.isNotNull(formats, "List of formats cannot be null");
        
        nameIDFormatPrecedenceLookupStrategy =
                FunctionSupport.constant(new ArrayList<>(StringSupport.normalizeStringCollection(formats)));
    }

    /**
     * Set a lookup strategy for the name identifier formats to use.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setNameIDFormatPrecedenceLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        nameIDFormatPrecedenceLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }
        
}
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

package net.shibboleth.idp.attribute.resolver.dc.rdbms.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.dc.ValidationException;
import net.shibboleth.idp.attribute.resolver.dc.Validator;
import net.shibboleth.idp.attribute.resolver.dc.impl.AbstractSearchDataConnector;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.ExecutableStatement;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.ResultMappingStrategy;
import net.shibboleth.idp.attribute.resolver.dc.rdbms.StringResultMappingStrategy;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link net.shibboleth.idp.attribute.resolver.DataConnector} that queries a relation database in order to retrieve
 * attribute data.
 */
public class RDBMSDataConnector extends AbstractSearchDataConnector<ExecutableStatement,ResultMappingStrategy> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RDBMSDataConnector.class);

    /** JDBC data source for retrieving {@link Connection}s. */
    private DataSource dataSource;

    /** Whether the default validator is being used. */
    private boolean defaultValidator = true;

    /** Whether the default mapping strategy is being used. */
    private boolean defaultMappingStrategy = true;

    /**
     * Constructor.
     */
    public RDBMSDataConnector() {
    }

    /**
     * Gets the JDBC data source for retrieving {@link Connection}s.
     * 
     * @return JDBC data source for retrieving {@link Connection}s
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the JDBC data source for retrieving {@link Connection}s.
     * 
     * @param source JDBC data source for retrieving {@link Connection}s
     */
    public void setDataSource(@Nonnull final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        dataSource = Constraint.isNotNull(source, "JDBC data source can not be null");
    }

    /** {@inheritDoc} */
    @Override public void setValidator(@Nonnull final Validator validator) {
        super.setValidator(validator);
        if (validator instanceof DataSourceValidator && dataSource != null) {
            ((DataSourceValidator) validator).setDataSource(dataSource);
        }
        defaultValidator = false;
    }

    /** {@inheritDoc} */
    @Override public void setMappingStrategy(@Nonnull final ResultMappingStrategy strategy) {
        super.setMappingStrategy(strategy);
        defaultMappingStrategy = false;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        if (dataSource == null) {
            throw new ComponentInitializationException(getLogPrefix() + " no data source was configured");
        }

        if (defaultValidator) {
            final DataSourceValidator validator = new DataSourceValidator();
            validator.setDataSource(dataSource);
            super.setValidator(validator);
        }
        if (defaultMappingStrategy) {
            super.setMappingStrategy(new StringResultMappingStrategy());
        }
        super.doInitialize();

        // validator should defer to data connector fail-fast-initialize during #initialize
        final boolean throwValidateError = getValidator().isThrowValidateError();
        try {
            getValidator().setThrowValidateError(isFailFastInitialize());
            getValidator().validate();
        } catch (final ValidationException e) {
            log.error("{} Invalid connector configuration", getLogPrefix(), e);
            if (isFailFastInitialize()) {
                throw new ComponentInitializationException(getLogPrefix() + " Invalid connector configuration", e);
            }
        } finally {
            getValidator().setThrowValidateError(throwValidateError);
        }
    }

    /**
     * Attempts to retrieve the attribute from the database.
     * 
     * @param statement statement used to retrieve data from the database
     * 
     * @return attributes gotten from the database
     * 
     * @throws ResolutionException thrown if there is a problem retrieving data from the database or transforming that
     *             data into {@link IdPAttribute}s
     */
    @Override
    @Nullable protected Map<String, IdPAttribute> retrieveAttributes(final ExecutableStatement statement)
            throws ResolutionException {

        if (statement == null) {
            throw new ResolutionException("Executable statement cannot be null");
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            try (final ResultSet queryResult = statement.execute(connection)) {
                log.trace("Data connector '{}': search returned {}", getId(), queryResult);
                return getMappingStrategy().map(queryResult);
            }
        } catch (final SQLException e) {
            throw new ResolutionException(getLogPrefix() + " Unable to execute SQL query", e);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (final SQLException e) {
                log.debug("{} Unable to close database connection; SQL State: {}, SQL Code: {}",
                        new Object[] {getLogPrefix(), e.getSQLState(), e.getErrorCode()}, e);
            }
        }
    }

}

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

package net.shibboleth.idp.admin.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;

import net.shibboleth.idp.Version;
import net.shibboleth.idp.module.IdPModule;
import net.shibboleth.idp.module.ModuleContext;
import net.shibboleth.idp.plugin.IdPPlugin;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * A bean that logs IdP internals when instantiated.
 * 
 * @since 4.3.0
 */
public final class LogImplementationDetails {

    /**
     * Log the IdP version and Java version and vendor at INFO level.
     * 
     * Log system properties defined by {@link StandardSystemProperty} at DEBUG level.
     * 
     * @param idpHomeLocation idp.home property
     */
    public LogImplementationDetails(@Nullable @NotEmpty final String idpHomeLocation) {
        
        final Logger logger = LoggerFactory.getLogger(LogImplementationDetails.class);
        logger.info("Shibboleth IdP Version {}", Version.getVersion());
        logger.info("Java version='{}' vendor='{}'", StandardSystemProperty.JAVA_VERSION.value(),
                StandardSystemProperty.JAVA_VENDOR.value());
        if (logger.isDebugEnabled()) {
            for (final StandardSystemProperty standardSystemProperty : StandardSystemProperty.values()) {
                logger.debug("{}", standardSystemProperty);
            }
        }
        final List<IdPPlugin> plugins = ServiceLoader.
                load(IdPPlugin.class).
                stream().
                map(e->e.get()).
                collect(Collectors.toList());
        if (plugins.isEmpty()) {
            logger.info("No Plugins Loaded");
        } else {
            logger.info("Plugins:");
            for (final IdPPlugin idpPlugin : plugins) {
                logger.info("\t\t{} : v{}.{}.{}",  idpPlugin.getPluginId(), idpPlugin.getMajorVersion(),
                        idpPlugin.getMinorVersion(), idpPlugin.getPatchVersion());
            }
        }
        Path idpHome;
        try {
            if (idpHomeLocation != null) {
                idpHome = Path.of(idpHomeLocation);
            } else {
                idpHome = null;
            }
        } catch (final RuntimeException e) {
            logger.info("Could not resolve idp.home from {} ", idpHomeLocation, e);
            idpHome = null;
        }

        if (idpHome != null) {
            final ModuleContext context = new ModuleContext(idpHome);
            final List<IdPModule> modules = ServiceLoader.
                    load(IdPModule.class).
                    stream().
                    map(e->e.get()).
                    filter(f->f.isEnabled(context)).
                    collect(Collectors.toList());
            if (modules.isEmpty()) {
                logger.info("No Modules Enabled");
            } else {
                logger.info("Enabled Modules:");
                for (final IdPModule module : modules) {
                    logger.info("\t\t{}",  module.getName(context));
                }
            }
        } else {
            logger.warn("Could not enumerate Modules");
        }
    }

}
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

package net.shibboleth.idp.log;

import javax.annotation.Nonnull;
import net.shibboleth.utilities.java.support.service.ReloadableService;

import org.springframework.core.io.Resource;

/**
 * A logging configuration abstraction that piggybacks on the {@link ReloadableService} interface.
 * 
 * <p>This interface is being moved to a supporting library.</p>
 * 
 * @deprecated
 */
@Deprecated(forRemoval=true, since="4.2.2")
public interface LoggingService extends ReloadableService<Object> {

    /**
     * Sets the logging configuration.
     * 
     * @param configuration logging configuration
     */
    void setLoggingConfiguration(@Nonnull final Resource configuration);

}
/**
 *
 * Copyright 2010 (C) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.papoose.log;

import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;


/**
 * @version $Revision: $ $Date: $
 */
public class LogReaderServiceImpl implements ServiceFactory
{
    private final static String CLASS_NAME = LogReaderServiceImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final LogServiceImpl logService;

    public LogReaderServiceImpl(LogServiceImpl logService)
    {
        if (logService == null) throw new IllegalArgumentException("LogService cannot be null");
        this.logService = logService;
    }

    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        LOGGER.entering(CLASS_NAME, "getService", new Object[]{ bundle, registration });

        Object service = new LogReaderServiceProxy(logService);

        LOGGER.exiting(CLASS_NAME, "getService", service);

        return service;
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        LOGGER.entering(CLASS_NAME, "ungetService", new Object[]{ bundle, registration, service });

        ((LogReaderServiceProxy) service).unregister();

        LOGGER.exiting(CLASS_NAME, "ungetService");
    }

}

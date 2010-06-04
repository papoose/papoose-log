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
public class LogReaderServiceFactory implements ServiceFactory
{
    private final static String CLASS_NAME = LogReaderServiceFactory.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final LogServiceImpl logService;

    public LogReaderServiceFactory(LogServiceImpl logService)
    {
        this.logService = logService;
    }

    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return new LogReaderServiceProxy(logService);
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        ((LogReaderServiceProxy)service).unregister();
    }

}

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

import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;


/**
 * @version $Revision: $ $Date: $
 */
public class LogReaderServiceProxy implements LogReaderService
{
    private final static String CLASS_NAME = LogReaderServiceProxy.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final LogServiceImpl logService;
    private final Set<LogListener> listeners = new CopyOnWriteArraySet<LogListener>();

    public LogReaderServiceProxy(LogServiceImpl logService)
    {
        this.logService = logService;
    }

    public void addLogListener(LogListener listener)
    {
        LOGGER.entering(CLASS_NAME, "addLogListener", listener);

        logService.addLogListener(listener);
        listeners.add(listener);

        LOGGER.exiting(CLASS_NAME, "addLogListener");
    }

    public void removeLogListener(LogListener listener)
    {
        LOGGER.entering(CLASS_NAME, "removeLogListener", listener);

        logService.removeLogListener(listener);
        listeners.remove(listener);

        LOGGER.exiting(CLASS_NAME, "removeLogListener");
    }

    public Enumeration getLog()
    {
        LOGGER.entering(CLASS_NAME, "getLog");

        Enumeration enumeration = logService.getLog();

        LOGGER.exiting(CLASS_NAME, "getLog", enumeration);

        return enumeration;
    }

    void unregister()
    {
        LOGGER.entering(CLASS_NAME, "unregister");

        for (LogListener listener : listeners) logService.removeLogListener(listener);
        listeners.clear();

        LOGGER.exiting(CLASS_NAME, "unregister");
    }
}

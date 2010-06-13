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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.papoose.core.Papoose;
import org.papoose.log.util.Util;


/**
 * @version $Revision: $ $Date: $
 */
public class PapooseBootLevelService
{
    private final static String CLASS_NAME = PapooseBootLevelService.class.getName();
    public final static String LOG_SERVICE_USE_PAPOOSE_THREAD_POOL = CLASS_NAME + ".usePapooseThreadPool";
    public final static String LOG_SERVICE_CORE_POOL_SIZE = CLASS_NAME + ".corePoolSize";
    public final static String LOG_SERVICE_MAX_POOL_SIZE = CLASS_NAME + ".maximumPoolSize";
    public final static String LOG_SERVICE_KEEP_ALIVE_TIME = CLASS_NAME + ".keepAliveTime";
    public final static String LOG_SERVICE_TIME_UNIT = CLASS_NAME + ".timeUnit";
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private volatile LogServiceImpl logService;
    private volatile ServiceRegistration logServiceRegistration;
    private volatile ServiceRegistration logReaderServiceRegistration;

    public void start(Papoose papoose)
    {
        LOGGER.entering(CLASS_NAME, "start", papoose);

        if (papoose == null) throw new IllegalArgumentException("Papoose instance is null");

        if (logService != null)
        {
            LOGGER.log(Level.WARNING, "Log service already started");
            return;
        }

        ExecutorService executor;

        if (papoose.getProperty(LOG_SERVICE_USE_PAPOOSE_THREAD_POOL) != null)
        {
            LOGGER.finest("Using Papoose's thread pool");

            executor = papoose.getExecutorService();
        }
        else
        {
            int corePoolSize = Util.parseInt(papoose.getProperty(LOG_SERVICE_CORE_POOL_SIZE), 1);
            int maximumPoolSize = Util.parseInt(papoose.getProperty(LOG_SERVICE_MAX_POOL_SIZE), 5);
            int keepAliveTime = Util.parseInt(papoose.getProperty(LOG_SERVICE_KEEP_ALIVE_TIME), 1);
            TimeUnit unit = Util.parseTimeUnit(papoose.getProperty(LOG_SERVICE_TIME_UNIT), TimeUnit.SECONDS);

            if (LOGGER.isLoggable(Level.FINEST))
            {
                LOGGER.finest("Creating own thread pool");
                LOGGER.finest("corePoolSize: " + corePoolSize);
                LOGGER.finest("maximumPoolSize: " + maximumPoolSize);
                LOGGER.finest("keepAliveTime: " + keepAliveTime);
                LOGGER.finest("unit: " + unit);
            }

            executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());
        }

        BundleContext bundleContext = papoose.getSystemBundleContext();

        logService = new LogServiceImpl(bundleContext, executor);

        logService.start();

        logServiceRegistration = bundleContext.registerService(LogService.class.getName(), logService, null);
        logReaderServiceRegistration = bundleContext.registerService(LogReaderService.class.getName(), new LogReaderServiceImpl(logService), null);

        LOGGER.exiting(CLASS_NAME, "start");
    }

    public void stop()
    {
        LOGGER.entering(CLASS_NAME, "stop");

        if (logService == null)
        {
            LOGGER.log(Level.WARNING, "Log service already stopped");
            return;
        }

        logServiceRegistration.unregister();
        logServiceRegistration = null;
        logReaderServiceRegistration.unregister();
        logReaderServiceRegistration = null;

        logService.stop();
        logService = null;

        LOGGER.exiting(CLASS_NAME, "stop");
    }
}

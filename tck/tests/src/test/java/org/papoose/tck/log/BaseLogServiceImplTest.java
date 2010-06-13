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
package org.papoose.tck.log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import org.ops4j.pax.exam.Inject;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import org.papoose.test.bundles.share.Share;
import org.papoose.test.bundles.share.ShareListener;


/**
 * @version $Revision: $ $Date: $
 */
public abstract class BaseLogServiceImplTest
{
    @Inject
    protected BundleContext bundleContext = null;
    protected ServiceReference shareReference;
    protected Share share;

    @Configuration
    public static Option[] baseConfigure()
    {
        return options(
                compendiumProfile(),
                provision(
                        mavenBundle().groupId("org.papoose.cmpn.tck.bundles").artifactId("bundle").version(asInProject()).start(false),
                        mavenBundle().groupId("org.papoose.test.bundles").artifactId("test-share").version(asInProject())
                )
        );
    }

    @Test
    public void test() throws Exception
    {
        assertNotNull(bundleContext);

        ServiceReference sr = bundleContext.getServiceReference(LogService.class.getName());
        LogService logService = (LogService) bundleContext.getService(sr);
        assertNotNull(logService);

        sr = bundleContext.getServiceReference(LogReaderService.class.getName());
        LogReaderService logReaderService = (LogReaderService) bundleContext.getService(sr);
        assertNotNull(logReaderService);

        final int NUM_LISTENERS = 100;
        final int NUM_MESSAGES = 1000;
        final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>();
        final AtomicInteger count = new AtomicInteger();
        final AtomicBoolean error = new AtomicBoolean(false);
        LogListener listener;
        logReaderService.addLogListener(listener = new LogListener()
        {
            int counter = 0;

            public void logged(LogEntry entry)
            {
                if (entry.getMessage().startsWith("Test"))
                {
                    error.set(error.get() || !("Test" + (counter++)).equals(entry.getMessage()));

                    count.incrementAndGet();
                    latch.get().countDown();
                }
            }
        });

        for (int i = 1; i < NUM_LISTENERS; i++)
        {
            logReaderService.addLogListener(new LogListener()
            {
                int counter = 0;

                public void logged(LogEntry entry)
                {
                    if (entry.getMessage().startsWith("Test"))
                    {
                        error.set(error.get() || !("Test" + (counter++)).equals(entry.getMessage()));
                        latch.get().countDown();
                    }
                }
            });
        }

        latch.set(new CountDownLatch(NUM_LISTENERS * NUM_MESSAGES));
        for (int i = 0; i < NUM_MESSAGES; i++) logService.log(LogService.LOG_INFO, "Test" + i);

        Enumeration enumeration = logReaderService.getLog();
        for (int i = 0; i < 100; i++)
        {
            LogEntry logEntry = (LogEntry) enumeration.nextElement();
            assertEquals("Test" + (999 - i), logEntry.getMessage());
        }

        assertFalse(enumeration.hasMoreElements());

        latch.get().await();

        assertEquals(NUM_MESSAGES, count.get());
        assertFalse(error.get());

        logReaderService.removeLogListener(listener);

        latch.set(new CountDownLatch((NUM_LISTENERS - 1) * NUM_MESSAGES));
        for (int i = 0; i < NUM_MESSAGES; i++) logService.log(LogService.LOG_INFO, "Test" + i);

        latch.get().await();

        assertEquals(NUM_MESSAGES, count.get());
    }

    @Test
    public void testBundleUnregsiter() throws Exception
    {
        final Map<String, Object> state = new HashMap<String, Object>();
        share.addListener(new ShareListener()
        {
            public void get(String key, Object value)
            {
            }

            public void put(String key, Object value)
            {
                state.put(key, value);
            }

            public void clear()
            {
            }
        });
        Bundle test = null;
        for (Bundle b : bundleContext.getBundles())
        {
            if ("org.papoose.cmpn.tck.bundle".equals(b.getSymbolicName()))
            {
                test = b;
                break;
            }
        }

        assertNotNull(test);

        test.start();

        ServiceReference sr = bundleContext.getServiceReference(LogService.class.getName());
        LogService logService = (LogService) bundleContext.getService(sr);
        assertNotNull(logService);

        logService.log(LogService.LOG_INFO, "TEST");

        Thread.sleep(100);

        LogEntry logEntry = (LogEntry) state.get("LOG");
        assertNotNull(logEntry);
        assertEquals("TEST", logEntry.getMessage());

        state.clear();
        test.stop();
        test.uninstall();

        logService.log(LogService.LOG_INFO, "STOPPED");

        assertEquals(null, state.get("STOPPED"));
    }

    @Before
    public void baseBefore()
    {
        shareReference = bundleContext.getServiceReference(Share.class.getName());
        share = (Share) bundleContext.getService(shareReference);
    }

    @After
    public void baseAfter()
    {
        bundleContext.ungetService(shareReference);
        shareReference = null;
        share = null;
    }
}

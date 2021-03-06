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
package org.papoose.log.util;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;


/**
 * @version $Revision: $ $Date: $
 */
public class EventAdminTracker extends ServiceTracker implements EventAdmin
{
    public EventAdminTracker(BundleContext context)
    {
        super(context, EventAdmin.class.getName(), null);
    }

    public void postEvent(Event event)
    {
        for (EventAdmin service : getEventAdminServices())
        {
            service.postEvent(event);
        }
    }

    public void sendEvent(Event event)
    {
        for (EventAdmin service : getEventAdminServices())
        {
            service.sendEvent(event);
        }
    }

    private final static EventAdmin[] EMPTY = new EventAdmin[0];

    private EventAdmin[] getEventAdminServices()
    {
        EventAdmin[] services = (EventAdmin[]) getServices();

        if (services == null) services = EMPTY;

        return services;
    }
}

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
package org.papoose.http;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;


/**
 * @version $Revision: $ $Date: $
 */
public class HttpServiceImpl implements HttpService
{
    private final static String CLASS_NAME = HttpServiceImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final static Properties EMPTY_PARAMS = new Properties();
    private final Object lock = new Object();
    private final Map<HttpContext, ServletContextImpl> contexts = new HashMap<HttpContext, ServletContextImpl>();
    private final Map<String, ServletRegistration> registrations = new HashMap<String, ServletRegistration>();
    private final BundleContext context;
    private final ServletDispatcher dispatcher;

    public HttpServiceImpl(BundleContext context, ServletDispatcher dispatcher)
    {
        if (context == null) throw new IllegalArgumentException("Bundle context is null");
        if (dispatcher == null) throw new IllegalArgumentException("Servlet dispatcher is null");

        this.context = context;
        this.dispatcher = dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    public void registerServlet(String alias, Servlet servlet, Dictionary initParams, HttpContext httpContext) throws ServletException, NamespaceException
    {
        ServletRegistration registration;
        ServletContextImpl servletContext;
        synchronized (lock)
        {
            if (registrations.containsKey(alias)) throw new NamespaceException("Alias " + alias + " already registered");

            if (httpContext == null) httpContext = createDefaultHttpContext();

            registration = new ServletRegistration(alias, servlet, httpContext);
            registrations.put(alias, registration);

            servletContext = contexts.get(httpContext);
            if (servletContext == null) contexts.put(httpContext, servletContext = new ServletContextImpl(httpContext));

            servletContext.incrementReferenceCount();
        }

        try
        {
            servlet.init(new ServletConfigImpl(alias, servletContext, initParams));

            dispatcher.register(registration);
        }
        catch (Throwable t)
        {
            LOGGER.log(Level.WARNING, "Error initializing servlet", t);

            synchronized (lock)
            {
                servletContext = contexts.get(httpContext);
                servletContext.decrementReferenceCount();
                if (servletContext.getReferenceCount() == 0) contexts.remove(registration.getContext());
            }

            if (t instanceof ServletException) throw (ServletException) t;
            throw (RuntimeException) t;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void registerResources(String alias, String name, HttpContext httpContext) throws NamespaceException
    {
        try
        {
            registerServlet(alias, new ServletWrapper(alias, name, httpContext), EMPTY_PARAMS, httpContext);
        }
        catch (ServletException se)
        {
            LOGGER.log(Level.SEVERE, "Error registering resource wrapper servlet", se);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregister(String alias)
    {
        ServletRegistration registration;
        synchronized (lock)
        {
            registration = registrations.remove(alias);

            if (registration == null) return;

            ServletContextImpl servletContext = contexts.get(registration.getContext());

            servletContext.decrementReferenceCount();
            if (servletContext.getReferenceCount() == 0) contexts.remove(registration.getContext());
        }

        try
        {
            dispatcher.unregister(registration);

            registration.getServlet().destroy();
        }
        catch (Throwable t)
        {
            LOGGER.log(Level.WARNING, "Error destroying servlet", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    public HttpContext createDefaultHttpContext()
    {
        return new HttpContext()
        {
            public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException
            {
                return true;
            }

            public URL getResource(String name)
            {
                if (name.startsWith("/")) name = name.substring(1);

                return context.getBundle().getResource(name);
            }

            public String getMimeType(String name)
            {
                return null;
            }
        };
    }
}

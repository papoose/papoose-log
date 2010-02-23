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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * @version $Revision: $ $Date: $
 */
public class FilterImpl implements Filter
{
    private final static String CLASS_NAME = FilterImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public void init(FilterConfig filterConfig) throws ServletException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void destroy()
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
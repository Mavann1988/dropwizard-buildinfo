/*
 * Copyright (C) 2014 Commerce Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commercehub.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BuildInfoServlet extends HttpServlet {

    final Logger log = LoggerFactory.getLogger(BuildInfoServlet.class);
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_PLAIN = "text/plain";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String ENCODING_UTF8 = "UTF-8";

    private final Properties properties = new Properties();
    private final ObjectWriter objectWriter = new ObjectMapper().writer();

    @Override
    public void init(ServletConfig config) {

        //load build information into properties
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    Attributes attributes = manifest.getMainAttributes();
                    Iterator iterator = attributes.keySet().iterator();
                    while (iterator.hasNext()) {
                        Attributes.Name attrName = (Attributes.Name) iterator.next();
                        properties.setProperty(attrName.toString(), attributes.getValue(attrName));
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Unable to retrieve build info", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(ENCODING_UTF8);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        response.setStatus(HttpServletResponse.SC_OK);

        String requestedKey = getRequestedKey(request);

        if (StringUtils.isNotBlank(requestedKey)) {
            writeKey(requestedKey, response);
        } else {
            if (requestedJson(request)) {
                writeAllJson(response);
            } else {
                writeAllHtml(response);
            }
        }
    }

    private void writeKey(String requestedKey, HttpServletResponse response) throws IOException {
        String value = properties.getProperty(requestedKey);
        if (StringUtils.isNotBlank(value)) {
            response.setContentType(CONTENT_TYPE_PLAIN);
            PrintWriter writer = response.getWriter();
            writer.print(value);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void writeAllJson(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        objectWriter.writeValue(response.getWriter(), properties);
    }

    private void writeAllHtml(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE_HTML);
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("  <body>");
        writer.println("    <h1>Info</h1>");
        writer.println("    <ul>");
        for (String key : properties.stringPropertyNames()) {
            writer.println("      <li>" + key + ": " + properties.get(key) + "</li>");
        }
        writer.println("    </ul>");
        writer.println("  </body>");
        writer.println("</html>");
    }

    private static String getRequestedKey(HttpServletRequest request) {
        return StringUtils.isNotBlank(request.getPathInfo()) && request.getPathInfo().length() > 1 ? request.getPathInfo().substring(1) : null;
    }

    private static boolean requestedJson(HttpServletRequest req) {
        return CONTENT_TYPE_JSON.equalsIgnoreCase(req.getHeader(HEADER_ACCEPT));
    }
}

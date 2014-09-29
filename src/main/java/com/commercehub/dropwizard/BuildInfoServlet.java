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
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BuildInfoServlet extends HttpServlet {

    final Logger log = LoggerFactory.getLogger(BuildInfoServlet.class);
    private static final String HEADER_ACCEPT = "Accept";
    private static final String ENCODING_UTF8 = "UTF-8";

    private final Properties manifestAttributes = new Properties();
    private final ObjectWriter objectWriter = new ObjectMapper().writer();

    private final List<String> blackListedManifestAttributes = new ArrayList<String>();

    BuildInfoServlet (){}

    BuildInfoServlet (List<String> blacklistedManifestAttributes){
        blackListedManifestAttributes.addAll(blacklistedManifestAttributes);
    }

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
                    while(iterator.hasNext()){
                        Attributes.Name attrName = (Attributes.Name) iterator.next();
                        if(!blackListedManifestAttributes.contains(attrName.toString())) {
                            manifestAttributes.setProperty(attrName.toString(), attributes.getValue(attrName));
                        }
                    }
                }
            }
        }
        catch (IOException e) {
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
            writeValueForKey(requestedKey, response);
        }
        else {
            if (requestedHtml(request)) {
                writeAllHtml(response);
            }
            else {
                writeAllJson(response);
            }
        }
    }

    private void writeValueForKey(String requestedKey, HttpServletResponse response) throws IOException {
        String value = manifestAttributes.getProperty(requestedKey);
        if (StringUtils.isNotBlank(value)) {
            response.setContentType(MediaType.TEXT_PLAIN);
            PrintWriter writer = response.getWriter();
            writer.print(value);
        }
        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void writeAllJson(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON);
        objectWriter.writeValue(response.getWriter(), manifestAttributes);
    }

    private void writeAllHtml(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.TEXT_HTML);
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("  <body>");
        writer.println("    <h1>Info</h1>");
        writer.println("    <ul>");
        for(String key : manifestAttributes.stringPropertyNames()){
            writer.println("      <li>" + key + ": " + manifestAttributes.get(key) + "</li>");
        }
        writer.println("    </ul>");
        writer.println("  </body>");
        writer.println("</html>");
    }

    private static String getRequestedKey(HttpServletRequest request) {
        return StringUtils.substring(request.getPathInfo(), 1);
    }

    private static boolean requestedHtml(HttpServletRequest req) {
        return StringUtils.isNotBlank(req.getHeader(HEADER_ACCEPT)) ? req.getHeader(HEADER_ACCEPT).contains(MediaType.TEXT_HTML) : false;
    }
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class BuildInfoServlet extends HttpServlet {

    final Logger log = LoggerFactory.getLogger(BuildInfoServlet.class);
    private static final String CONTENT_TYPE = "application/json";
    private ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) {
        mapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        response.setStatus(HttpServletResponse.SC_OK);
        try (OutputStream output = response.getOutputStream()) {
            getWriter(request).writeValue(output, getBuildInfo());
        }
    }

    private Map<String, String> getBuildInfo() {
        Map<String, String> buildInfo = new HashMap<>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            if (resources != null) {
                while (resources.hasMoreElements()) {
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    for (BuildInfoManifestAttribute entry : BuildInfoManifestAttribute.values()) {
                        String key = entry.getKey();
                        String value = manifest.getMainAttributes().getValue(key);
                        if (value != null) {
                            buildInfo.put(key, value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Unable to retrieve build info", e);
        }
        return buildInfo;
    }

    private ObjectWriter getWriter(HttpServletRequest request) {
        boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
        return prettyPrint ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
    }

}

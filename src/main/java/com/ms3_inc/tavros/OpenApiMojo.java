/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ms3_inc.tavros;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Goal which generates the RoutesGenerated and RoutesImplementation using the Groovy script.
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.INITIALIZE )

public class OpenApiMojo extends AbstractMojo {
    private final static Logger LOGGER = Logger.getLogger(RoutesCreator.class.getName());

    @Parameter( defaultValue = "${project}", required = true )
    private MavenProject project;

    @Parameter( property = "specificationUri", required = true )
    private String specificationUri;

    public void execute() throws MojoExecutionException {
        String baseDir = project.getBasedir().getAbsolutePath();
        String groupId = project.getGroupId();

        RoutesCreator routesCreator = new RoutesCreator(specificationUri, baseDir, groupId);
        routesCreator.init();
    }

}

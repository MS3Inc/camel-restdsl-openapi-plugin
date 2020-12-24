package com.ms3.camel;

// TODO: Fix package to be com.ms3-inc

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Goal which generates the RoutesGenerated and RoutesImplementation using the Groovy script.
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.INITIALIZE )

public class OpenApiMojo extends AbstractMojo {
    @Parameter( defaultValue = "${project}", required = true )
    private MavenProject project;

    @Parameter( property = "specificationUri", required = true )
    private String specificationUri;

    public void execute() throws MojoExecutionException {
        String baseDir = project.getBasedir().getAbsolutePath();
        String groupId = project.getGroupId();

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("generate-from-spec.groovy");

        try {
            String groovyScript = IOUtils.toString(in, StandardCharsets.UTF_8);
            Binding binding = new Binding();
            binding.setProperty("oasPathStr", specificationUri);
            binding.setProperty("baseDir", baseDir);
            binding.setProperty("groupId", groupId);
            GroovyShell shell = new GroovyShell(binding);

            shell.evaluate(groovyScript);
        } catch (IOException e) {
            getLog().error(e.getMessage());
        }
    }

}

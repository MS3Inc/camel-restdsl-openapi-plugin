package com.ms3_inc.camel;

// TODO: Fix package to be com.ms3-inc

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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

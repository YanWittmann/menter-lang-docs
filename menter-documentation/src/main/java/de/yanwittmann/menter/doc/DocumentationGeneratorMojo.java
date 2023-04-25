package de.yanwittmann.menter.doc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Mojo(name = "generate-documentation")
public class DocumentationGeneratorMojo extends AbstractMojo {

    @Parameter(required = true)
    private File guideBaseDir;

    @Parameter(required = true)
    private File targetBaseDir;

    @Parameter(required = true)
    private File templateFile;

    @Parameter(required = true)
    private File structureFile;

    /**
     * The directory where the Menter source project is located.
     */
    @Parameter(required = true)
    private Map<String, String> externalProperties;

    public void execute() throws MojoExecutionException {
        try {
            getLog().info("guideBaseDir: " + guideBaseDir.getAbsolutePath());
            getLog().info("targetBaseDir: " + targetBaseDir.getAbsolutePath());
            getLog().info("templateFile: " + templateFile.getAbsolutePath());
            getLog().info("structureFile: " + structureFile.getAbsolutePath());
            getLog().info("externalProperties: " + externalProperties);

            DocumentationGenerator.generate(guideBaseDir, targetBaseDir, templateFile, structureFile, externalProperties);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate the documentation for Menter: " + e.getMessage(), e);
        }
    }
}

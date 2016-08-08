package net.sf.lightairmp;

import net.sf.lightair.Api;
import net.sf.lightair.internal.Keywords;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generate XSD from database.
 */
@Mojo(name = "generate-xsd")
public class GenerateXsdMojo extends AbstractMojo implements Keywords {

	/**
	 * Path to Light Air properties file. Defaults to {@code target/test-classes/light-air.properties}.
	 */
	@Parameter(defaultValue = "target/test-classes/light-air.properties")
	private String lightAirProperties;

	// Standard getters and setters for the properties

	public String getLightAirProperties() {
		return lightAirProperties;
	}

	public void setLightAirProperties(String lightAirProperties) {
		this.lightAirProperties = lightAirProperties;
	}

	/**
	 * Generate the XSD files.
	 */
	public void execute() throws MojoFailureException {
		getLog().info("Generating Light Air XSD files...");

		Api.initialize(lightAirProperties);
		Api.generateXsd();
		Api.shutdown();

		getLog().info("Finished generating Light Air XSD files.");
	}
}

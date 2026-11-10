package net.sf.lightairmp;

import net.sf.lightair.LightAirApi;
import net.sf.lightair.internal.Keywords;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generate XSD from the database.
 */
@Mojo(name = "generate-xsd")
public class GenerateXsdMojo extends AbstractMojo implements Keywords {

	/**
	 * Path to the Light Air properties file. Defaults to {@code target/test-classes/light-air.properties}.
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

		LightAirApi.initialize(lightAirProperties);
		LightAirApi.generateXsd();
		LightAirApi.shutdown();

		getLog().info("Finished generating Light Air XSD files.");
	}
}

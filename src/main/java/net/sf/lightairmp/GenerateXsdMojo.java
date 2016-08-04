package net.sf.lightairmp;

import net.sf.lightair.internal.Api;
import net.sf.lightair.internal.properties.PropertyKeys;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Generate XSD from database.
 *
 * @goal generate-xsd
 */
public class GenerateXsdMojo extends AbstractMojo implements PropertyKeys {

	/**
	 * Path to Light Air properties file. Defaults to {@code light-air.properties}.
	 *
	 * @parameter property="light-air.properties"
	 * @required
	 */
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

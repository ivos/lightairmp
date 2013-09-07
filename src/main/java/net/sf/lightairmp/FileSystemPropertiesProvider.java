package net.sf.lightairmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sf.lightair.internal.properties.PropertiesProvider;

import org.apache.maven.plugin.MojoFailureException;

public class FileSystemPropertiesProvider extends PropertiesProvider {

	private final File lightAirProperties;

	public FileSystemPropertiesProvider(File lightAirProperties) {
		this.lightAirProperties = lightAirProperties;
	}

	public void initFromFileSystem() throws MojoFailureException {
		try {
			getProperties().load(new FileInputStream(lightAirProperties));
		} catch (FileNotFoundException e) {
			throw new MojoFailureException(
					"Cannot find LightAir properties file: "
							+ lightAirProperties, e);
		} catch (IOException e) {
			throw new MojoFailureException(
					"Cannot read LightAir properties file: "
							+ lightAirProperties, e);
		}
	}

}

package net.sf.lightairmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import net.sf.lightair.internal.properties.PropertiesProvider;
import net.sf.lightair.internal.util.Profiles;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class FileSystemPropertiesProvider extends PropertiesProvider {

	private final File lightAirProperties;
	private final Log log;

	public FileSystemPropertiesProvider(Log log, File lightAirProperties) {
		this.lightAirProperties = lightAirProperties;
		this.log = log;
	}

	public void initFromFileSystem() throws MojoFailureException {
		log.debug("Loading main Light Air properties file...");
		loadPropertiesForProfileFromFileSystem(Profiles.DEFAULT_PROFILE,
				lightAirProperties);

		log.debug("Loading profiles properties...");
		loadPropertiesForProfilesFromFileSystem(lightAirProperties
				.getAbsoluteFile().getParentFile());
	}

	private void loadPropertiesForProfileFromFileSystem(String profile,
			File propertiesFile) throws MojoFailureException {
		try {
			log.debug("Loading properties file for profile [" + profile
					+ "] from path " + propertiesFile.getCanonicalPath());
			getProfileProperties(profile).load(
					new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e) {
			throw new MojoFailureException("Cannot find properties file: "
					+ propertiesFile, e);
		} catch (IOException e) {
			throw new MojoFailureException("Cannot read properties file: "
					+ propertiesFile, e);
		}
	}

	private void loadPropertiesForProfilesFromFileSystem(File baseDir)
			throws MojoFailureException {
		final String profilePrefix = "profile.";
		final Set<String> profiles = getPropertyKeysWithPrefix(
				Profiles.DEFAULT_PROFILE, profilePrefix);
		for (String key : profiles) {
			String profileName = key.substring(profilePrefix.length());
			String profilePropertiesFileName = getProperty(
					Profiles.DEFAULT_PROFILE, key);
			properties.put(profileName, new Properties());
			loadPropertiesForProfileFromFileSystem(profileName, new File(
					baseDir, profilePropertiesFileName));
		}
	}

}

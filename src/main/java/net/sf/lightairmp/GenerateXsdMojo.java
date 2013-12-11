package net.sf.lightairmp;

import static org.unitils.thirdparty.org.apache.commons.io.IOUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import net.sf.lightair.exception.CreateDatabaseConnectionException;
import net.sf.lightair.internal.properties.PropertiesProvider;
import net.sf.lightair.internal.properties.PropertyKeys;
import net.sf.lightairmp.dbmaintainer.XsdDataSetStructureGenerator;
import net.sf.lightairmp.exception.DatabaseDriverClassNotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.unitils.core.ConfigurationLoader;
import org.unitils.core.UnitilsException;
import org.unitils.core.dbsupport.DbSupportFactory;
import org.unitils.core.dbsupport.DefaultSQLHandler;
import org.unitils.core.dbsupport.SQLHandler;

/**
 * Generate XSD from database.
 * 
 * @goal generate-xsd
 */
public class GenerateXsdMojo extends AbstractMojo implements PropertyKeys {

	/**
	 * Path to Light Air properties file. Defaults to
	 * {@code target/test-classes/light-air.properties}.
	 * 
	 * @parameter expression=
	 *            "${project.build.testOutputDirectory}/light-air.properties"
	 * @required
	 */
	private File lightAirProperties;

	/**
	 * The directory into which the XSD files will be generated. Defaults to
	 * {@code src/test/java}.
	 * 
	 * @parameter expression= "${project.build.testSourceDirectory}"
	 * @required
	 */
	private File xsdDir;

	private PropertiesProvider propertiesProvider;

	// Standard getters and setters for the properties

	public File getXsdDir() {
		return xsdDir;
	}

	public void setXsdDir(File xsdDir) {
		this.xsdDir = xsdDir;
	}

	public File getLightAirProperties() {
		return lightAirProperties;
	}

	public void setLightAirProperties(File lightAirProperties) {
		this.lightAirProperties = lightAirProperties;
	}

	public PropertiesProvider getPropertiesProvider() {
		return propertiesProvider;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

	/**
	 * Generate the XSD files.
	 */
	public void execute() throws MojoFailureException {
		getLog().info("Generating DbUnit flat dataset XSD files...");

		propertiesProvider = getLightairPropertiesProvider();

		for (String profile : propertiesProvider.getProfileNames()) {
			processProfile(profile);
		}
		generateLightAirTypes();

		getLog().info("Finished generating DbUnit flat dataset XSD files.");
	}

	private void processProfile(String profile) throws MojoFailureException {
		getLog().debug("Generating XSD for profile [" + profile + "]");

		Properties configuration = getUnitilsConfiguration(profile);

		DataSource dataSource = createDataSource(profile);
		SQLHandler sqlHandler = new DefaultSQLHandler(dataSource, false);

		XsdDataSetStructureGenerator generator = new XsdDataSetStructureGenerator(
				profile);
		generator.init(configuration, sqlHandler);
		generator.generateDataSetStructure();
	}

	private PropertiesProvider getLightairPropertiesProvider()
			throws MojoFailureException {
		FileSystemPropertiesProvider propertiesProvider = new FileSystemPropertiesProvider(
				getLog(), lightAirProperties);
		propertiesProvider.initFromFileSystem();
		return propertiesProvider;
	}

	/**
	 * Create Unitils configuration.
	 * <p>
	 * Load Unitils default values. Add support for H2 database. Finally, set
	 * our own configuration: database dialect, schema names and output
	 * directory.
	 * 
	 * @return
	 * @throws MojoFailureException
	 */
	private Properties getUnitilsConfiguration(String profile)
			throws MojoFailureException {
		Properties configuration = new Properties();
		loadUnitilsDefaultValues(configuration);
		addSupportForH2Database(configuration);
		addSupportForInformix(configuration);

		configuration.put(DbSupportFactory.PROPKEY_DATABASE_DIALECT,
				propertiesProvider.getProperty(profile, DATABASE_DIALECT));
		configuration.put(DbSupportFactory.PROPKEY_DATABASE_SCHEMA_NAMES,
				propertiesProvider.getProperty(profile, SCHEMA_NAMES));
		configuration.put(XsdDataSetStructureGenerator.PROPKEY_XSD_DIR_NAME,
				getXsdDirCanonical());

		return configuration;
	}

	/**
	 * Convert XSD output directory into canonical file name.
	 * 
	 * @return
	 * @throws MojoFailureException
	 */
	private String getXsdDirCanonical() throws MojoFailureException {
		try {
			final String canonicalPath = xsdDir.getCanonicalPath();
			getLog().debug(
					"Expanded XSD directory specification [" + xsdDir.getPath()
							+ "] as [" + canonicalPath + "].");
			return canonicalPath;
		} catch (IOException e) {
			throw new MojoFailureException(
					"Cannot find specified XSD directory: " + xsdDir.getPath(),
					e);
		}
	}

	/**
	 * Load default Unitils values.
	 * 
	 * @param configuration
	 */
	private void loadUnitilsDefaultValues(Properties configuration) {
		new ConfigurationLoader() {
			@Override
			public void loadDefaultConfiguration(Properties properties) {
				super.loadDefaultConfiguration(properties);
			}
		}.loadDefaultConfiguration(configuration);
	}

	/**
	 * Add custom support for H2 database into Unitils configuration, as Unitils
	 * does not support it out-of-the-box.
	 * 
	 * @param configuration
	 */
	private void addSupportForH2Database(Properties configuration) {
		configuration.put(
				"org.unitils.core.dbsupport.DbSupport.implClassName.h2",
				"org.unitils.core.dbsupport.H2DbSupport");
		configuration.put(
				"org.dbunit.dataset.datatype.IDataTypeFactory.implClassName",
				"org.dbunit.ext.h2.H2DataTypeFactory");
		configuration.put("database.identifierQuoteString.h2", "auto");
		configuration.put("database.storedIndentifierCase.h2", "auto");
	}

	/**
	 * Add custom support for Informix into Unitils configuration, as Unitils
	 * does not support it out-of-the-box.
	 * 
	 * @param configuration
	 */
	private void addSupportForInformix(Properties configuration) {
		configuration.put(
				"org.unitils.core.dbsupport.DbSupport.implClassName.informix",
				"org.unitils.core.dbsupport.InformixDbSupport");
		configuration.put(
				"org.dbunit.dataset.datatype.IDataTypeFactory.implClassName",
				"org.dbunit.dataset.datatype.DefaultDataTypeFactory");
		configuration.put("database.identifierQuoteString.informix", "auto");
		configuration.put("database.storedIndentifierCase.informix", "auto");
	}

	/**
	 * Create a datasource to connect to the database.
	 * 
	 * @param profile
	 * @return
	 */
	private DataSource createDataSource(String profile) {
		SingleConnectionDataSource dataSource;
		getLog().info(
				"Creating database connection for profile [" + profile + "]...");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String driverClassName = getProperty(profile, DRIVER_CLASS_NAME);
		try {
			Class.forName(driverClassName);
			final String url = getProperty(profile, CONNECTION_URL);
			final String username = getProperty(profile, USER_NAME);
			final String password = getProperty(profile, PASSWORD);
			Connection connection = DriverManager.getConnection(url, username,
					password);
			dataSource = new SingleConnectionDataSource(connection, true);
			stopWatch.stop();
			getLog().debug(
					"Connection driver=[" + driverClassName + "], url=[" + url
							+ "], username=[" + username + "], password=["
							+ password + "].");
			getLog().debug(
					"Created database connection for profile [" + profile
							+ "] in " + stopWatch.getTime() + " ms.");
		} catch (ClassNotFoundException e) {
			throw new DatabaseDriverClassNotFoundException(driverClassName, e);
		} catch (SQLException e) {
			throw new CreateDatabaseConnectionException(e);
		}
		return dataSource;
	}

	private String getProperty(String profile, String key) {
		return propertiesProvider.getProperty(profile, key);
	}

	public void generateLightAirTypes() {
		Writer writer = null;
		final File file = new File(xsdDir, "light-air-types.xsd");
		try {
			writer = new BufferedWriter(new FileWriter(file));
			InputStream inputStream = getClass().getClassLoader()
					.getResourceAsStream("light-air-types.xsd");
			IOUtils.copy(inputStream, writer);
		} catch (Exception e) {
			throw new UnitilsException("Error generating xsd file: " + file, e);
		} finally {
			closeQuietly(writer);
		}
	}

}

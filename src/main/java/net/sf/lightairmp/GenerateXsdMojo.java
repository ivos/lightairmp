package net.sf.lightairmp;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import net.sf.lightair.exception.CreateDatabaseConnectionException;
import net.sf.lightair.exception.DatabaseDriverClassNotFoundException;
import net.sf.lightair.internal.properties.PropertiesProvider;
import net.sf.lightair.internal.properties.PropertyKeys;

import org.apache.commons.lang.time.StopWatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.unitils.core.ConfigurationLoader;
import org.unitils.core.dbsupport.DbSupportFactory;
import org.unitils.core.dbsupport.DefaultSQLHandler;
import org.unitils.core.dbsupport.SQLHandler;
import org.unitils.dbmaintainer.structure.impl.XsdDataSetStructureGenerator;

/**
 * Generate XSD from database.
 * 
 * @goal generate-xsd
 */
public class GenerateXsdMojo extends AbstractMojo implements PropertyKeys {

	/**
	 * The directory into which the XSD files will be generated. Defaults to
	 * {@code src/test/resources}.
	 * 
	 * @parameter expression="${project.build.testSourceDirectory}"
	 * @required
	 */
	private File xsdDir;

	public void setXsdDir(File xsdDir) {
		this.xsdDir = xsdDir;
	}

	public void execute() throws MojoFailureException {
		PropertiesProvider propertiesProvider = getLightairPropertiesProvider();
		Properties configuration = getUnitilsConfiguration(propertiesProvider);

		DataSource dataSource = createDataSource(propertiesProvider);
		SQLHandler sqlHandler = new DefaultSQLHandler(dataSource, false);

		XsdDataSetStructureGenerator generator = new XsdDataSetStructureGenerator();
		generator.init(configuration, sqlHandler);
		generator.generateDataSetStructure();
	}

	private PropertiesProvider getLightairPropertiesProvider() {
		PropertiesProvider propertiesProvider = new PropertiesProvider();
		propertiesProvider.init();
		return propertiesProvider;
	}

	private Properties getUnitilsConfiguration(
			PropertiesProvider propertiesProvider) throws MojoFailureException {
		Properties configuration = new Properties();
		loadUnitilsDefaultValues(configuration);
		addSupportForH2Database(configuration);

		configuration.put(DbSupportFactory.PROPKEY_DATABASE_DIALECT,
				propertiesProvider.getProperty(DATABASE_DIALECT));
		configuration.put(DbSupportFactory.PROPKEY_DATABASE_SCHEMA_NAMES,
				propertiesProvider.getProperty(SCHEMA_NAMES));
		configuration.put(XsdDataSetStructureGenerator.PROPKEY_XSD_DIR_NAME,
				getXsdDirCanonical());

		return configuration;
	}

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

	private void loadUnitilsDefaultValues(Properties configuration) {
		new ConfigurationLoader() {
			@Override
			public void loadDefaultConfiguration(Properties properties) {
				super.loadDefaultConfiguration(properties);
			}
		}.loadDefaultConfiguration(configuration);
	}

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

	private DataSource createDataSource(PropertiesProvider propertiesProvider) {
		SingleConnectionDataSource dataSource;
		getLog().info("Creating database connection.");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String driverClassName = propertiesProvider
				.getProperty(DRIVER_CLASS_NAME);
		try {
			Class.forName(driverClassName);
			Connection connection = DriverManager.getConnection(
					propertiesProvider.getProperty(CONNECTION_URL),
					propertiesProvider.getProperty(USER_NAME),
					propertiesProvider.getProperty(PASSWORD));
			dataSource = new SingleConnectionDataSource(connection, true);
			stopWatch.stop();
			getLog().debug(
					"Created database connection in " + stopWatch.getTime()
							+ " ms.");
		} catch (ClassNotFoundException e) {
			throw new DatabaseDriverClassNotFoundException(driverClassName, e);
		} catch (SQLException e) {
			throw new CreateDatabaseConnectionException(e);
		}
		return dataSource;
	}

}

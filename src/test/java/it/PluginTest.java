package it;

import net.sf.lightairmp.GenerateXsdMojo;
import net.sf.seaf.test.util.TemplatingTestBase;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.Mojo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;

public class PluginTest extends TemplatingTestBase {

	static boolean replaceTemplates = false;

	static final String GENERATED_BASE_DIR = "target/generated-xsd/light-air-xsd/";

	static JdbcTemplate db, dbHsql;

	static {
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:h2:mem:test", "sa", "", true);
		db = new JdbcTemplate(dataSource);

		SingleConnectionDataSource dataSourceHsql = new SingleConnectionDataSource("jdbc:hsqldb:mem:test", "sa", "", true);
		dbHsql = new JdbcTemplate(dataSourceHsql);
	}

	public PluginTest() {
		super(replaceTemplates, DEFAULT_TEMPLATES_BASE_DIR, GENERATED_BASE_DIR);
	}

	private void createDefaultSchema() {
		db.execute("create table table2 (id int primary key, table1id int, a2 varchar(255), int2 int)");
		db.execute("create table table1 (id int primary key, a1 varchar(255), int1 int, "
				+ "double1 double, date1 date, time1 time, timestamp1 timestamp)");
	}

	private void dropDefaultSchema() {
		db.execute("drop table table2 if exists");
		db.execute("drop table table1 if exists");
	}

	private void createOtherSchema() {
		db.execute("create schema schema2");
		db.execute("create table schema2.table33 (id int primary key, a6 varchar(255), double6 double)");
		db.execute("create table schema2.table32 (id int primary key, a5 varchar(255), date5 date)");
		db.execute("create table schema2.table31 (id int primary key, a4 varchar(255), int4 int, time4 time)");
	}

	private void dropOtherSchema() {
		db.execute("drop table schema2.table31 if exists");
		db.execute("drop table schema2.table31 if exists");
		db.execute("drop table schema2.table31 if exists");
		db.execute("drop schema schema2 if exists");
	}

	private void createHsql() {
		dbHsql.execute("create table tableHsql (id int primary key, tableHsqlid int, aHsql varchar(255), intHsql int)");
	}

	private void dropHsql() {
		dbHsql.execute("drop table tableHsql if exists");
	}

	private void verifyProfiles() throws Exception {
		performTest("profiles/light-air-types.xsd", "light-air-types.xsd");
		performTest("profiles/dataset.xsd", "dataset.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "hsql", "");

		performTest("profiles/dataset-hsql.xsd", "dataset-hsql.xsd");
		performTest("profiles/PUBLIC-hsql.xsd", "PUBLIC-hsql.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "hsql", "-hsql");
	}

	private void validateXsdConformance(String path, String suffix)
			throws Exception {
		Source xmlFile = new StreamSource(new File(path + "/sample" + suffix
				+ ".xml"));
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(new File(path + "/dataset"
				+ suffix + ".xsd"));
		Validator validator = schema.newValidator();
		try {
			validator.validate(xmlFile);
		} catch (SAXException e) {
			Assert.fail("Sample XML file is not valid by generated XSD. "
					+ e.getLocalizedMessage());
		}
	}

	private Mojo createMojo(String propertiesFileName) {
		final GenerateXsdMojo mojo = new GenerateXsdMojo();
		mojo.setLightAirProperties(propertiesFileName);
		return mojo;
	}

	@Before
	public void setup() {
		FileUtils.deleteQuietly(new File(GENERATED_BASE_DIR));
	}

	@Test
	public void defaultSchemaOnly() throws Exception {
		try {
			createDefaultSchema();

			createMojo("light-air.properties").execute();
		} finally {
			dropDefaultSchema();
		}

		performTest("defaultSchemaOnly/light-air-types.xsd", "light-air-types.xsd");
		performTest("defaultSchemaOnly/dataset.xsd", "dataset.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "defaultSchemaOnly", "");
	}

	@Test
	public void profiles() throws Exception {
		try {
			createDefaultSchema();
			createHsql();
			createOtherSchema();

			createMojo("light-air-profiles.properties").execute();
		} finally {
			dropHsql();
			dropDefaultSchema();
			dropOtherSchema();
		}

		performTest("profiles/light-air-types.xsd", "light-air-types.xsd");
		performTest("profiles/dataset.xsd", "dataset.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "profiles", "");

		performTest("profiles/dataset-schema2.xsd", "dataset-schema2.xsd");

		performTest("profiles/dataset-hsql.xsd", "dataset-hsql.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "profiles", "-hsql");
	}
}

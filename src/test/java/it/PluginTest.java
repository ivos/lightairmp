package it;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.lightairmp.GenerateXsdMojo;
import net.sf.seaf.test.util.TemplatingTestBase;

import org.apache.maven.plugin.Mojo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.xml.sax.SAXException;

public class PluginTest extends TemplatingTestBase {

	static boolean replaceTemplates = false;

	static final String GENERATED_BASE_DIR = "target/generated-xsd/light-air-xsd/";

	static JdbcTemplate db, dbHsql;

	static {
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource(
				"jdbc:h2:mem:test", "sa", "", true);
		db = new JdbcTemplate(dataSource);

		SingleConnectionDataSource dataSourceHsql = new SingleConnectionDataSource(
				"jdbc:hsqldb:mem:test", "sa", "", true);
		dbHsql = new JdbcTemplate(dataSourceHsql);
	}

	public PluginTest() {
		super(replaceTemplates, DEFAULT_TEMPLATES_BASE_DIR, GENERATED_BASE_DIR);
	}

	private void createDefaultSchema() {
		db.execute("create table table1 (id int primary key, a1 varchar(255), int1 int, "
				+ "double1 double, date1 date, time1 time, timestamp1 timestamp)");
		db.execute("create table table2 (id int primary key, table1id int, a2 varchar(255), int2 int)");
	}

	private void dropDefaultSchema() {
		db.execute("drop table table1 if exists");
		db.execute("drop table table2 if exists");
	}

	private void createOtherSchemas() {
		db.execute("create schema schema1");
		db.execute("create schema schema2");
		db.execute("create schema schema3");
		db.execute("create table schema1.table11 (id int primary key, a1 varchar(255), int1 int, "
				+ "double1 double, date1 date, time1 time, timestamp1 timestamp)");
		db.execute("create table schema1.table12 (id int primary key, table1id int, a2 varchar(255), int2 int)");
		db.execute("create table schema2.table21 (id int primary key, a3 varchar(255), int3 int)");
		db.execute("create table schema3.table31 (id int primary key, a4 varchar(255), int4 int, time4 time)");
		db.execute("create table schema3.table32 (id int primary key, a5 varchar(255), date5 date)");
		db.execute("create table schema3.table33 (id int primary key, a6 varchar(255), double6 double)");
	}

	private void dropOtherSchemas() {
		db.execute("drop table schema1.table11 if exists");
		db.execute("drop table schema1.table12 if exists");
		db.execute("drop table schema2.table21 if exists");
		db.execute("drop table schema3.table31 if exists");
		db.execute("drop table schema3.table31 if exists");
		db.execute("drop table schema3.table31 if exists");
		db.execute("drop schema schema1 if exists");
		db.execute("drop schema schema2 if exists");
		db.execute("drop schema schema3 if exists");
	}

	private void createHsql() {
		dbHsql.execute("create table tableHsql (id int primary key, tableHsqlid int, aHsql varchar(255), intHsql int)");
	}

	private void dropHsql() {
		dbHsql.execute("drop table tableHsql if exists");
	}

	private void verify(String test) throws Exception {
		performTest(test + "/light-air-types.xsd", "light-air-types.xsd");

		performTest(test + "/dataset.xsd", "dataset.xsd");
		performTest(test + "/PUBLIC.xsd", "PUBLIC.xsd");
		performTest(test + "/SCHEMA1.xsd", "SCHEMA1.xsd");
		performTest(test + "/SCHEMA2.xsd", "SCHEMA2.xsd");
		performTest(test + "/SCHEMA3.xsd", "SCHEMA3.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + test, "");
	}

	private void verifyHsql() throws Exception {
		performTest("hsql/light-air-types.xsd", "light-air-types.xsd");

		performTest("hsql/dataset.xsd", "dataset.xsd");
		performTest("hsql/PUBLIC.xsd", "PUBLIC.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + "hsql", "");

		performTest("hsql/dataset-hsql.xsd", "dataset-hsql.xsd");
		performTest("hsql/PUBLIC-hsql.xsd", "PUBLIC-hsql.xsd");
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
		mojo.setXsdDir(new File(GENERATED_BASE_DIR));
		mojo.setLightAirProperties(new File("src/test/resources/"
				+ propertiesFileName));
		return mojo;
	}

	@Test
	public void defaultSchemaOnly() throws Exception {
		try {
			createDefaultSchema();

			createMojo("light-air.properties").execute();
		} finally {
			dropDefaultSchema();
		}

		verify("defaultSchemaOnly");
	}

	@Test
	public void otherSchemasOnly() throws Exception {
		try {
			createOtherSchemas();

			createMojo("light-air.properties").execute();
		} finally {
			dropOtherSchemas();
		}

		verify("otherSchemasOnly");
	}

	@Test
	public void mixed() throws Exception {
		try {
			createDefaultSchema();
			createOtherSchemas();

			createMojo("light-air.properties").execute();
		} finally {
			dropOtherSchemas();
			dropDefaultSchema();
		}

		verify("mixed");
	}

	@Test
	public void profiles() throws Exception {
		try {
			createDefaultSchema();
			createHsql();

			createMojo("light-air-profiles.properties").execute();
		} finally {
			dropHsql();
			dropDefaultSchema();
		}

		verifyHsql();
	}

}

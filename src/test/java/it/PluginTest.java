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

	static JdbcTemplate db;

	static {
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource(
				"jdbc:h2:mem:test", "sa", "", true);
		db = new JdbcTemplate(dataSource);
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

	private void verify(String test) throws Exception {
		performTest(test + "/dataset.xsd", "dataset.xsd");
		performTest(test + "/PUBLIC.xsd", "PUBLIC.xsd");
		performTest(test + "/SCHEMA1.xsd", "SCHEMA1.xsd");
		performTest(test + "/SCHEMA2.xsd", "SCHEMA2.xsd");
		performTest(test + "/SCHEMA3.xsd", "SCHEMA3.xsd");
		validateXsdConformance(DEFAULT_TEMPLATES_BASE_DIR + test);
	}

	private void validateXsdConformance(String path) throws Exception {
		Source xmlFile = new StreamSource(new File(path + "/sample.xml"));
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory
				.newSchema(new File(path + "/dataset.xsd"));
		Validator validator = schema.newValidator();
		try {
			validator.validate(xmlFile);
		} catch (SAXException e) {
			Assert.fail("Sample XML file is not valid by generated XSD. "
					+ e.getLocalizedMessage());
		}
	}

	private Mojo createMojo() {
		final GenerateXsdMojo mojo = new GenerateXsdMojo();
		mojo.setXsdDir(new File(GENERATED_BASE_DIR));
		mojo.setLightAirProperties(new File(
				"src/test/resources/light-air.properties"));
		return mojo;
	}

	@Test
	public void defaultSchemaOnly() throws Exception {
		try {
			createDefaultSchema();

			createMojo().execute();
		} finally {
			dropDefaultSchema();
		}

		verify("defaultSchemaOnly");
	}

	@Test
	public void otherSchemasOnly() throws Exception {
		try {
			createOtherSchemas();

			createMojo().execute();
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

			createMojo().execute();
		} finally {
			dropOtherSchemas();
			dropDefaultSchema();
		}

		verify("mixed");
	}

}

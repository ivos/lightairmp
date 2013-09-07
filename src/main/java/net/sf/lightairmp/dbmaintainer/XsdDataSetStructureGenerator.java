package net.sf.lightairmp.dbmaintainer;

import static org.unitils.thirdparty.org.apache.commons.io.IOUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;

import org.unitils.core.UnitilsException;
import org.unitils.core.dbsupport.DbSupport;
import org.unitils.util.PropertyUtils;

public class XsdDataSetStructureGenerator extends
		org.unitils.dbmaintainer.structure.impl.XsdDataSetStructureGenerator {

	private String complexTypeSuffix;

	@Override
	protected void doInit(Properties configuration) {
		super.doInit(configuration);
		complexTypeSuffix = PropertyUtils.getString(
				PROPKEY_XSD_COMPLEX_TYPE_SUFFIX, configuration);
	}

	@Override
	protected void generateDataSetXsd(File xsdDirectory) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(xsdDirectory,
					"dataset.xsd")));

			String defaultSchemaName = defaultDbSupport.getSchemaName();
			writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			writer.write("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" xmlns:dflt=\""
					+ defaultSchemaName + "\">\n");

			for (DbSupport dbSupport : dbSupports) {
				String schemaName = dbSupport.getSchemaName();
				writer.write("\t<xsd:import namespace=\"" + schemaName
						+ "\" schemaLocation=\"" + schemaName + ".xsd\" />\n");
			}

			writer.write("\t<xsd:element name=\"dataset\">\n");
			writer.write("\t\t<xsd:complexType>\n");
			writer.write("\t\t\t<xsd:choice minOccurs=\"0\" maxOccurs=\"unbounded\">\n");

			Set<String> defaultSchemaTableNames = defaultDbSupport
					.getTableNames();
			for (String tableName : defaultSchemaTableNames) {
				writer.write("\t\t\t\t<xsd:element name=\"" + tableName
						+ "\" type=\"dflt:" + tableName + complexTypeSuffix
						+ "\" />\n");
			}

			// FIX START allow any element from any DB schema namespace
			for (DbSupport dbSupport : dbSupports) {
				String schemaName = dbSupport.getSchemaName();
				writer.write("\t\t\t\t<xsd:any namespace=\"" + schemaName
						+ "\" />\n");
			}
			// FIX END

			writer.write("\t\t\t</xsd:choice>\n");
			writer.write("\t\t</xsd:complexType>\n");
			writer.write("\t</xsd:element>\n");
			writer.write("</xsd:schema>\n");

		} catch (Exception e) {
			throw new UnitilsException("Error generating xsd file: "
					+ xsdDirectory, e);
		} finally {
			closeQuietly(writer);
		}
	}

	public String getComplexTypeSuffix() {
		return complexTypeSuffix;
	}

}

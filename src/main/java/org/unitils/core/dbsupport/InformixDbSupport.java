package org.unitils.core.dbsupport;

import java.util.Set;

public class InformixDbSupport extends DbSupport {

	public InformixDbSupport() {
		super("informix");
	}

	@Override
	public Set<String> getTableNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select tabname from systables where owner = '"
						+ getSchemaName() + "' and tabtype = 'T'");
	}

	@Override
	public Set<String> getColumnNames(String tableName) {
		return getSQLHandler()
				.getItemsAsStringSet(
						"select colname from syscolumns, systables where syscolumns.tabid = systables.tabid and tabname = '"
								+ tableName
								+ "' and systables.owner = '"
								+ getSchemaName() + "'");
	}

	@Override
	public Set<String> getViewNames() {
		return getSQLHandler().getItemsAsStringSet(
				"select tabname from systables where owner = '"
						+ getSchemaName() + "' and tabtype = 'V'");
	}

	@Override
	public void disableReferentialConstraints() {
	}

	@Override
	public void disableValueConstraints() {
	}

}

package de.fmk.kicknrush.db;

import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @author FabianK
 */
abstract class AbstractDBHandler<K, V> implements IDBHandler<K, V> {
	static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
	static final String SELECT_ALL_FROM            = "SELECT * FROM ";
	static final String WHERE                      = " WHERE ";


	int mergeInto(final JdbcTemplate jdbcTemplate,
	              final String       tableName,
	              final String[]     keyColumns,
	              final Object[]     values) {
		final StringBuilder queryBuilder;
		final StringBuilder valuesBuilder;

		queryBuilder  = new StringBuilder("MERGE INTO ");
		valuesBuilder = new StringBuilder(") VALUES(");

		queryBuilder.append(tableName).append(" KEY (");

		for (int i = 0; i < keyColumns.length; i++) {
			queryBuilder.append(keyColumns[i]);

			if (i + 1 < keyColumns.length)
				queryBuilder.append(", ");
		}

		for (int i = 0; i < values.length; i++) {
			valuesBuilder.append("?");

			if (i + 1 < values.length) {
				valuesBuilder.append(",");
			}
		}

		valuesBuilder.append(");");
		queryBuilder.append(valuesBuilder.toString());

		return jdbcTemplate.update(queryBuilder.toString(), values);
	}


	int insertInto(final JdbcTemplate jdbcTemplate,
	               final String       table,
	               final String[]     columns,
	               final Object[]     values) {
		final StringBuilder queryBuilder;
		final StringBuilder valuesBuilder;

		queryBuilder  = new StringBuilder("INSERT INTO ");
		valuesBuilder = new StringBuilder(") VALUES(");

		queryBuilder.append(table).append("(");

		for (int i = 0; i < columns.length; i++) {
			queryBuilder.append(columns[i]);
			valuesBuilder.append("?");

			if (i + 1 < columns.length) {
				queryBuilder.append(", ");
				valuesBuilder.append(",");
			}
		}

		valuesBuilder.append(");");
		queryBuilder.append(valuesBuilder.toString());

		return jdbcTemplate.update(queryBuilder.toString(), values);
	}
}

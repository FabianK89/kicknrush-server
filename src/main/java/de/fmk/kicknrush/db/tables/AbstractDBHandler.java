package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
abstract class AbstractDBHandler<K, V> implements IDBHandler<K, V> {
	static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
	static final String SELECT_ALL_FROM            = "SELECT * FROM ";
	static final String WHERE                      = " WHERE ";


	int deleteByID(final JdbcTemplate jdbcTemplate, final String table, final String idColumn, final K id) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("DELETE FROM ").append(table).append(WHERE).append(idColumn).append("=?;");

		return jdbcTemplate.update(queryBuilder.toString(), id);
	}


	int mergeInto(final JdbcTemplate  jdbcTemplate,
	              final String        tableName,
	              final String[]      keyColumns,
	              final ColumnValue[] values) {
		final List<Object>  valueList;
		final StringBuilder queryBuilder;
		final StringBuilder keyBuilder;
		final StringBuilder valuesBuilder;

		valueList     = new ArrayList<>();
		queryBuilder  = new StringBuilder("MERGE INTO ");
		keyBuilder    = new StringBuilder(") KEY (");
		valuesBuilder = new StringBuilder(") VALUES(");

		queryBuilder.append(tableName).append("(");

		for (int i = 0; i < keyColumns.length; i++) {
			keyBuilder.append(keyColumns[i]);

			if (i + 1 < keyColumns.length)
				keyBuilder.append(", ");
		}

		for (int i = 0; i < values.length; i++) {
			valueList.add(values[i].getValue());
			queryBuilder.append(values[i].getName());
			valuesBuilder.append("?");

			if (i + 1 < values.length) {
				queryBuilder.append(", ");
				valuesBuilder.append(",");
			}
		}

		valuesBuilder.append(");");
		keyBuilder.append(valuesBuilder.toString());
		queryBuilder.append(keyBuilder.toString());

		return jdbcTemplate.update(queryBuilder.toString(), valueList.toArray(new Object[0]));
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

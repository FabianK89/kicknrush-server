package de.fmk.kicknrush.db;


import de.fmk.kicknrush.models.User;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;


public class DatabaseHandler {
	private JdbcTemplate jdbcTemplate;


	public DatabaseHandler(JdbcTemplate template) {
		jdbcTemplate = template;
	}


	public void createInitialTables() {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
		            .append(DBConstants.TBL_NAME_USER)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USERNAME).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_PWD).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_IS_ADMIN).append(" BOOLEAN);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	public List<User> getAllUsers() {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT * FROM ").append(DBConstants.TBL_NAME_USER);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) ->
				new User(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN),
				         rs.getString(DBConstants.COL_NAME_PWD),
				         rs.getString(DBConstants.COL_NAME_USERNAME),
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID)));
	}


	public boolean addNewUser(final String username, final String password, final boolean admin) {
		final String[]      columnNames;
		final StringBuilder queryBuilder;
		final StringBuilder valuesBuilder;

		columnNames = new String[]{DBConstants.COL_NAME_ID,
		                           DBConstants.COL_NAME_USERNAME,
		                           DBConstants.COL_NAME_PWD,
		                           DBConstants.COL_NAME_IS_ADMIN};

		queryBuilder  = new StringBuilder("INSERT INTO ");
		valuesBuilder = new StringBuilder(") VALUES(");

		queryBuilder.append(DBConstants.TBL_NAME_USER).append("(");

		for (int i = 0; i < columnNames.length; i++) {
			queryBuilder.append(columnNames[i]);
			valuesBuilder.append("?");

			if (i + 1 < columnNames.length) {
				queryBuilder.append(", ");
				valuesBuilder.append(",");
			}
		}

		valuesBuilder.append(");");
		queryBuilder.append(valuesBuilder.toString());

		jdbcTemplate.update(queryBuilder.toString(), UUID.randomUUID(), username, password, admin);
		return false;
	}


	public User findUser(final String username, final String password) {
		final List<User>    resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT * FROM ").append(DBConstants.TBL_NAME_USER)
		            .append(" WHERE ").append(DBConstants.COL_NAME_USERNAME).append("=? AND ")
		            .append(DBConstants.COL_NAME_PWD).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[]{username, password}, (rs, rowNum) ->
				new User(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN),
				         rs.getString(DBConstants.COL_NAME_PWD),
				         rs.getString(DBConstants.COL_NAME_USERNAME),
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

package de.fmk.kicknrush.db;


import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	private JdbcTemplate jdbcTemplate;


	public DatabaseHandler(JdbcTemplate template) {
		jdbcTemplate = template;
	}


	public void createInitialTables() {
		final List<String> tables;

		tables = new ArrayList<>(jdbcTemplate.query("SELECT * FROM INFORMATION_SCHEMA.TABLES",
		                                            (rs, rowNum) -> rs.getString("TABLE_NAME")));

		if (!tables.contains(DBConstants.TBL_NAME_USER)) {
			final String password;
			final String salt;

			createUserTable();

			salt     = PasswordUtils.getSalt(255);
			password = PasswordUtils.generateSecurePassword("admin123", salt);

			addNewUser("Admin", password, salt, true);
		}
	}


	private void createUserTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the user table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
		            .append(DBConstants.TBL_NAME_USER)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USERNAME).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_PWD).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_SALT).append(" VARCHAR(255), ")
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
				         rs.getString(DBConstants.COL_NAME_SALT),
				         rs.getString(DBConstants.COL_NAME_USERNAME),
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID)));
	}


	public boolean addNewUser(final String username, final String password, final String salt, final boolean admin) {
		final int           createdRows;
		final String[]      columnNames;
		final StringBuilder queryBuilder;
		final StringBuilder valuesBuilder;

		columnNames = new String[]{DBConstants.COL_NAME_ID,
		                           DBConstants.COL_NAME_USERNAME,
		                           DBConstants.COL_NAME_PWD,
		                           DBConstants.COL_NAME_SALT,
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

		createdRows = jdbcTemplate.update(queryBuilder.toString(), UUID.randomUUID(), username, password, salt, admin);

		if (createdRows == 1) {
			LOGGER.info("Created the user with name '{}'.", username);
			return true;
		}

		return false;
	}


	public User findUser(final String username) {
		final List<User>    resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT * FROM ").append(DBConstants.TBL_NAME_USER)
		            .append(" WHERE ").append(DBConstants.COL_NAME_USERNAME).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[]{ username }, (rs, rowNum) ->
				new User(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN),
				         rs.getString(DBConstants.COL_NAME_PWD),
				         rs.getString(DBConstants.COL_NAME_SALT),
				         rs.getString(DBConstants.COL_NAME_USERNAME),
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

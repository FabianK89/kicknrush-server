package de.fmk.kicknrush.db;


import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.h2.api.TimestampWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
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
			createUserTable();
			addNewUser("Admin", "admin123", null, true);
		}

		if (!tables.contains(DBConstants.TBL_NAME_SESSION))
			createSessionTable();
	}


	private void createSessionTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the session table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
		            .append(DBConstants.TBL_NAME_SESSION)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USER_ID).append(" UUID UNIQUE, ")
		            .append(DBConstants.COL_NAME_LOGGED_IN).append(" TIMESTAMP WITH TIME ZONE, ")
		            .append(DBConstants.COL_NAME_LAST_ACTION).append(" TIMESTAMP WITH TIME ZONE);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	private void createUserTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the user table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
		            .append(DBConstants.TBL_NAME_USER)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USERNAME).append(" VARCHAR(255) UNIQUE, ")
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
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID),
				         null));
	}


	public boolean addSession(final User user) {
		final int                   createdRows;
		final LocalDateTime         now;
		final Object[]              values;
		final String[]              columnNames;
		final TimestampWithTimeZone timestamp;
		final UUID                  sessionID;
		final ZonedDateTime         zdt;
		final ZoneId                zoneID;

		zoneID      = TimeZone.getTimeZone("UTC").toZoneId();
		now         = LocalDateTime.now(zoneID);
		zdt         = now.atZone(zoneID);
		timestamp   = new TimestampWithTimeZone(zdt.toInstant().toEpochMilli(), 0, (short) 0);
		sessionID   = UUID.randomUUID();
		columnNames = new String[] { DBConstants.COL_NAME_ID,
		                             DBConstants.COL_NAME_USER_ID,
		                             DBConstants.COL_NAME_LOGGED_IN,
		                             DBConstants.COL_NAME_LAST_ACTION };
		values      = new Object[] { sessionID, user.getId(), timestamp, timestamp };
		createdRows = insertInto(DBConstants.TBL_NAME_SESSION, columnNames, values);

		if (createdRows == 1) {
			user.setSessionID(sessionID);
			LOGGER.info("Session has been created for the user with id '{}'.", user.getId());
			return true;
		}

		return false;
	}


	public boolean closeSession(final String userID) {
		final int  deleteRows;
		final UUID id;

		id         = UUID.fromString(userID);
		deleteRows = deleteByID(DBConstants.TBL_NAME_SESSION, DBConstants.COL_NAME_USER_ID, id);

		if (deleteRows == 1) {
			LOGGER.info("Session of user with id '{}' has been closed.", userID);
			return true;
		}

		return false;
	}


	private int deleteByID(final String table, final String idColumn, final UUID id) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("DELETE FROM ").append(table).append(" WHERE ").append(idColumn).append("=?;");

		return jdbcTemplate.update(queryBuilder.toString(), id);
	}


	private int insertInto(final String table, final String[] columns, final Object[] values) {
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


	public boolean addNewUser(final String username, final String password, final String salt, final boolean admin) {
		final int      createdRows;
		final Object[] values;
		final String[] columnNames;

		columnNames = new String[] { DBConstants.COL_NAME_ID,
		                             DBConstants.COL_NAME_USERNAME,
		                             DBConstants.COL_NAME_PWD,
		                             DBConstants.COL_NAME_SALT,
		                             DBConstants.COL_NAME_IS_ADMIN };
		values      = new Object[] { UUID.randomUUID(), username, password, salt, admin };
		createdRows = insertInto(DBConstants.TBL_NAME_USER, columnNames, values);

		if (createdRows == 1) {
			LOGGER.info("The user with name '{}' has been created.", username);
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
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID),
				         null));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

package de.fmk.kicknrush.db;


import de.fmk.kicknrush.models.Session;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.utils.TimeUtils;
import org.h2.api.TimestampWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	private static final String SELECT_ALL_FROM = "SELECT * FROM ";
	private static final String WHERE           = " WHERE ";

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


	public List<String> getUsernames() {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_USERNAME)
		            .append(" FROM ").append(DBConstants.TBL_NAME_USER);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> rs.getString(DBConstants.COL_NAME_USERNAME));
	}


	public List<User> getUsers() {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_USER);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> {
			final User user = new User();

			user.setId((UUID) rs.getObject(DBConstants.COL_NAME_ID));
			user.setUsername(rs.getString(DBConstants.COL_NAME_USERNAME));
			user.setAdmin(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN));

			return user;
		});
	}


	public boolean addSession(final User user) {
		final int                   createdRows;
		final Object[]              values;
		final String[]              columnNames;
		final TimestampWithTimeZone timestamp;
		final UUID                  sessionID;

		timestamp   = TimeUtils.createTimestamp(LocalDateTime.now());
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


	public Session getSessionForID(final String sessionID) {
		final List<Session> sessionList;
		final StringBuilder queryBuilder;

		if (sessionID == null || sessionID.isEmpty())
			return null;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_SESSION)
		            .append(WHERE).append(DBConstants.COL_NAME_ID).append("=?;");

		sessionList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { sessionID }, (rs, rowNum) -> {
			final Session session = new Session();

			TimestampWithTimeZone timestamp;

			session.setSessionID((UUID) rs.getObject(DBConstants.COL_NAME_ID));
			session.setUserID((UUID) rs.getObject(DBConstants.COL_NAME_USER_ID));

			timestamp = (TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_LOGGED_IN);
			session.setLoggedInTime(TimeUtils.convertTimestamp(timestamp));

			timestamp = (TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_LAST_ACTION);
			session.setLastActionTime(TimeUtils.convertTimestamp(timestamp));

			return session;
		});

		if (sessionList == null || sessionList.isEmpty())
			return null;

		return sessionList.get(0);
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
		queryBuilder.append("DELETE FROM ").append(table).append(WHERE).append(idColumn).append("=?;");

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


	public boolean updateUser(final UUID userID, final String username, final String password, final String salt) {
		final int      updatedRows;
		final Object[] values;
		final String[] columns;

		if (userID == null || username == null || password == null || salt == null)
			return false;

		columns     = new String[] { DBConstants.COL_NAME_USERNAME, DBConstants.COL_NAME_PWD, DBConstants.COL_NAME_SALT };
		values      = new Object[] { username, password, salt, userID };
		updatedRows = updateForID(DBConstants.TBL_NAME_USER, columns, values);

		if (updatedRows == 1) {
			LOGGER.info("The user with id '{}' was updated.", userID);
			return true;
		}

		return false;
	}


	public boolean updateSession(final UUID sessionID) {
		final int      updatedRows;
		final Object[] values;
		final String[] columns;

		if (sessionID == null)
			return false;

		columns     = new String[] { DBConstants.COL_NAME_LAST_ACTION };
		values      = new Object[] { TimeUtils.createTimestamp(LocalDateTime.now()), sessionID };
		updatedRows = updateForID(DBConstants.TBL_NAME_SESSION, columns, values);

		if (updatedRows == 1) {
			LOGGER.info("The session with id '{}' was updated.", sessionID);
			return true;
		}

		return false;
	}


	private int updateForID(final String table, final String[] columns, final Object[] values) {
		final StringBuilder setBuilder;
		final StringBuilder queryBuilder;

		setBuilder   = new StringBuilder(" SET ");
		queryBuilder = new StringBuilder("UPDATE ");

		queryBuilder.append(table);

		for (int i = 0; i < columns.length; i++) {
			setBuilder.append(columns[i]).append("=?");

			if (i + 1 < columns.length)
				setBuilder.append(", ");
		}

		queryBuilder.append(setBuilder.toString()).append(WHERE).append(DBConstants.COL_NAME_ID).append("=?;");

		return jdbcTemplate.update(queryBuilder.toString(), values);
	}


	public User findUser(final String username) {
		final List<User>    resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_USER)
		            .append(WHERE).append(DBConstants.COL_NAME_USERNAME).append("=?;");

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

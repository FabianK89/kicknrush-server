package de.fmk.kicknrush.db;


import de.fmk.kicknrush.db.tables.SessionHandler;
import de.fmk.kicknrush.db.tables.TeamHandler;
import de.fmk.kicknrush.db.tables.UpdateHandler;
import de.fmk.kicknrush.db.tables.UserHandler;
import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Session;
import de.fmk.kicknrush.models.Team;
import de.fmk.kicknrush.models.Update;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author FabianK
 */
@Component
public class DatabaseHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHandler.class);

	private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
	private static final String SELECT_ALL_FROM            = "SELECT * FROM ";
	private static final String UUID_PRIMARY_KEY           = " UUID PRIMARY KEY, ";
	private static final String WHERE                      = " WHERE ";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private SessionHandler sessionHandler;
	private TeamHandler    teamHandler;
	private UpdateHandler  updateHandler;
	private UserHandler    userHandler;


	public DatabaseHandler() {
		sessionHandler = new SessionHandler();
		updateHandler  = new UpdateHandler();
		userHandler    = new UserHandler(updateHandler);
		teamHandler    = new TeamHandler(updateHandler);
	}


	public void createInitialTables() {
		final List<String> tables;

		tables = new ArrayList<>(jdbcTemplate.query("SELECT * FROM INFORMATION_SCHEMA.TABLES",
		                                            (rs, rowNum) -> rs.getString("TABLE_NAME")));

		if (!tables.contains(DBConstants.TBL_NAME_UPDATES))
			updateHandler.createTable(jdbcTemplate);

		if (!tables.contains(DBConstants.TBL_NAME_USER)) {
			userHandler.createTable(jdbcTemplate);
			createUser(new User(true, null, "Admin", UUID.randomUUID()));
		}

		if (!tables.contains(DBConstants.TBL_NAME_SESSION))
			sessionHandler.createTable(jdbcTemplate);

		if (!tables.contains(DBConstants.TBL_NAME_TEAM))
			teamHandler.createTable(jdbcTemplate);

		if (!tables.contains(DBConstants.TBL_NAME_GROUP))
			createGroupsTable();

		if (!tables.contains(DBConstants.TBL_NAME_MATCH))
			createMatchesTable();

		if (!tables.contains(DBConstants.TBL_NAME_BET))
			createBetsTable();
	}


	public boolean isAdminSession(final UUID sessionID) {
		final Session session;
		final User    user;

		if (sessionID == null)
			return false;

		session = sessionHandler.findByID(jdbcTemplate, sessionID);

		if (session == null)
			return false;

		user = userHandler.findByID(jdbcTemplate, session.getUserID());

		if (user == null)
			return false;

		return user.isAdmin();
	}


	public List<Update> getUpdates() {
		return updateHandler.getValues(jdbcTemplate);
	}


	private void createBetsTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the bets table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_BET)
		            .append("(").append(DBConstants.COL_NAME_USER_ID).append(" UUID NOT NULL, ")
		            .append(DBConstants.COL_NAME_MATCH_ID).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_HOME_GOALS).append(" INTEGER, ")
		            .append(DBConstants.COL_NAME_TEAM_GUEST_GOALS).append(" INTEGER, ")
		            .append(" PRIMARY KEY(").append(DBConstants.COL_NAME_USER_ID).append(",")
		            .append(DBConstants.COL_NAME_MATCH_ID).append("))");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	private void createGroupsTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the groups table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_GROUP)
		            .append("(").append(DBConstants.COL_NAME_GROUP_ID).append(" INTEGER PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_GROUP_NAME).append(" VARCHAR(255) NOT NULL, ")
		            .append(DBConstants.COL_NAME_GROUP_ORDER_ID).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_YEAR).append(" INTEGER NOT NULL);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	private void createMatchesTable() {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the matches table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_MATCH)
		            .append("(").append(DBConstants.COL_NAME_MATCH_ID).append(" INTEGER PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_MATCH_OVER).append(" BOOLEAN, ")
		            .append(DBConstants.COL_NAME_KICKOFF).append(" TIMESTAMP WITH TIME ZONE, ")
		            .append(DBConstants.COL_NAME_GROUP_ID).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_GUEST).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_HOME).append(" INTEGER NOT NULL);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	public List<String> getUsernames() {
		return userHandler.getUsernames(jdbcTemplate);
	}


	public List<Team> getTeams() {
		return teamHandler.getValues(jdbcTemplate);
	}


	public List<Integer> getTeamIDs() {
		return teamHandler.getIDs(jdbcTemplate);
	}


	public List<Integer> getMatchIDs() {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_MATCH_ID)
		            .append(" FROM ").append(DBConstants.TBL_NAME_MATCH);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> rs.getInt(DBConstants.COL_NAME_MATCH_ID));
	}


	public List<Integer> getGroupIDsForYear(final int year) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_GROUP_ID)
		            .append(" FROM ").append(DBConstants.TBL_NAME_GROUP)
		            .append(WHERE).append(DBConstants.COL_NAME_YEAR).append("=?;");

		return jdbcTemplate.query(queryBuilder.toString(), new Object[] { year }, (rs, rowNum) ->
				rs.getInt(DBConstants.COL_NAME_TEAM_ID));
	}


	public List<User> getUsers() {
		return userHandler.getValues(jdbcTemplate);
	}


	public boolean addGroup(final Group group) {
		final int      createdRows;
		final Object[] values;
		final String[] columnNames;

		columnNames = new String[] { DBConstants.COL_NAME_GROUP_ID,
		                             DBConstants.COL_NAME_GROUP_NAME,
		                             DBConstants.COL_NAME_GROUP_ORDER_ID,
		                             DBConstants.COL_NAME_YEAR };
		values      = new Object[] { group.getGroupID(), group.getGroupName(), group.getGroupOrderID(), group.getYear() };
		createdRows = insertInto(DBConstants.TBL_NAME_GROUP, columnNames, values);

		if (createdRows == 1) {
			LOGGER.info("The group with id '{}' and name '{}' has been created.", group.getGroupID(), group.getGroupName());
			return true;
		}

		return false;
	}


	public boolean addTeam(final Team team) {
		return teamHandler.merge(jdbcTemplate, team);
	}


	public boolean addMatch(final Match match) {
		final int      createdRows;
		final Object[] values;
		final String[] columnNames;

		columnNames = new String[] { DBConstants.COL_NAME_MATCH_ID,
		                             DBConstants.COL_NAME_MATCH_OVER,
		                             DBConstants.COL_NAME_KICKOFF,
		                             DBConstants.COL_NAME_GROUP_ID,
		                             DBConstants.COL_NAME_TEAM_GUEST,
		                             DBConstants.COL_NAME_TEAM_HOME };
		values      = new Object[] { match.getMatchID(),
		                             match.isMatchIsFinished(),
		                             TimeUtils.createTimestamp(match.getMatchDateTimeUTC()),
		                             match.getGroup().getGroupID(),
		                             match.getTeam2().getTeamId(),
		                             match.getTeam1().getTeamId() };
		createdRows = insertInto(DBConstants.TBL_NAME_MATCH, columnNames, values);

		if (createdRows == 1) {
			LOGGER.info("The match with id '{}' has been created.", match.getMatchID());
			return true;
		}

		return false;
	}


	public UUID createSession(final UUID userID) {
		final LocalDateTime now;
		final Session       session;

		now     = LocalDateTime.now(Clock.systemUTC());
		session = new Session(now, now, UUID.randomUUID(), userID);

		if (sessionHandler.merge(jdbcTemplate, session))
			return session.getSessionID();

		return null;
	}


	public boolean checkSession(final UUID sessionID, final UUID userID) {
		final Session session;

		if (userID == null || sessionID == null)
			return false;

		session = sessionHandler.findByID(jdbcTemplate, sessionID);

		return session != null && userID.equals(session.getUserID());
	}


	public boolean closeSession(final UUID sessionID, final String userID) {
		if (sessionHandler.deleteByID(jdbcTemplate, sessionID)) {
			LOGGER.info("Session of user with id '{}' has been closed.", userID);
			return true;
		}

		return false;
	}


	public boolean updateSession(final UUID sessionID) {
		final LocalDateTime now;
		final Session       session;

		session = sessionHandler.findByID(jdbcTemplate, sessionID);
		now     = LocalDateTime.now(Clock.systemUTC());

		if (session == null)
			return false;

		session.setLastActionTime(now);

		return sessionHandler.merge(jdbcTemplate, session);
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


	public boolean createUser(final User user) {
		return userHandler.merge(jdbcTemplate, user);
	}


	public boolean deleteUser(final UUID userID) {
		if (userHandler.deleteByID(jdbcTemplate, userID)) {
			LOGGER.info("The user with id '{}' has been deleted.", userID);
			return true;
		}

		return false;
	}


	public boolean administrateUser(final User user) {
		final int      updatedRows;
		final Object[] values;
		final String[] columns;

		if (user == null)
			return false;

		columns     = new String[] { DBConstants.COL_NAME_IS_ADMIN };
		values      = new Object[] { user.isAdmin(), user.getId() };
		updatedRows = updateForID(DBConstants.TBL_NAME_USER, columns, values);

		if (updatedRows == 1) {
			LOGGER.info("The user with id '{}' was updated.", user.getId());
			return true;
		}

		return false;
	}


	public boolean updateUser(final User user) {
		return userHandler.merge(jdbcTemplate, user);
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
		final User user;

		if (username == null)
			throw new IllegalArgumentException("The username must not be null.");

		user = userHandler.findByUsername(jdbcTemplate, username);

		if (user == null)
			LOGGER.info("Could not find the user with name '{}'.", username);

		return user;
	}


	public Team findTeam(final int teamID) {
		final Team team;

		team = teamHandler.findByID(jdbcTemplate, teamID);

		if (team == null)
			LOGGER.info("Could not find the team with id '{}'.", teamID);

		return team;
	}


	public Group findGroup(final int groupID) {
		final List<Group>    resultList;
		final StringBuilder  queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_GROUP)
		            .append(WHERE).append(DBConstants.COL_NAME_GROUP_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[]{ groupID }, (rs, rowNum) ->
				new Group(rs.getInt(DBConstants.COL_NAME_GROUP_ID),
				          rs.getInt(DBConstants.COL_NAME_GROUP_ORDER_ID),
				          rs.getInt(DBConstants.COL_NAME_YEAR),
				          rs.getString(DBConstants.COL_NAME_GROUP_NAME)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

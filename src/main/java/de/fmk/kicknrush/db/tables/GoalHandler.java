package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
public class GoalHandler extends AbstractDBHandler<Integer, Goal> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GoalHandler.class);


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the goals table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_GOAL)
		            .append("(").append(DBConstants.COL_NAME_MATCH_ID).append(" INTEGER UNIQUE, ")
		            .append(DBConstants.COL_NAME_SCORE_TEAM_HOME).append(" INTEGER DEFAULT 0, ")
		            .append(DBConstants.COL_NAME_SCORE_TEAM_GUEST).append(" INTEGER DEFAULT 0, ")
		            .append(DBConstants.COL_NAME_GOAL_GETTER_ID).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_GOAL_GETTER_NAME).append(" VARCHAR(255) NOT NULL, ")
		            .append(DBConstants.COL_NAME_MATCH_MINUTE).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_IS_OVERTIME).append(" BOOLEAN DEFAULT FALSE, ")
		            .append(DBConstants.COL_NAME_IS_OWN_GOAL).append(" BOOLEAN DEFAULT FALSE, ")
		            .append(DBConstants.COL_NAME_IS_PENALTY).append(" BOOLEAN DEFAULT FALSE, ")
		            .append("PRIMARY KEY(").append(DBConstants.COL_NAME_MATCH_ID).append(", ")
		            .append(DBConstants.COL_NAME_SCORE_TEAM_HOME).append(", ")
		            .append(DBConstants.COL_NAME_SCORE_TEAM_GUEST).append("));");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	@Override
	public List<Integer> getIDs(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching the IDs only is not supported.");
	}


	@Override
	public List<Goal> getValues(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching all values is not supported.");
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Goal goal) {
		final int               mergedRows;
		final List<ColumnValue> values;
		final String[]          keyColumns;

		if (goal == null) {
			LOGGER.warn("MERGE FAILED: The goal parameter is null.");
			return false;
		}

		keyColumns = new String[] { DBConstants.COL_NAME_MATCH_ID,
		                            DBConstants.COL_NAME_SCORE_TEAM_HOME,
		                            DBConstants.COL_NAME_SCORE_TEAM_GUEST };
		values     = new ArrayList<>();

		values.add(new ColumnValue(DBConstants.COL_NAME_MATCH_ID, goal.getMatchID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_SCORE_TEAM_HOME, goal.getScoreTeam1()));
		values.add(new ColumnValue(DBConstants.COL_NAME_SCORE_TEAM_GUEST, goal.getScoreTeam2()));
		values.add(new ColumnValue(DBConstants.COL_NAME_GOAL_GETTER_ID, goal.getGoalGetterID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_GOAL_GETTER_NAME, goal.getGoalGetterName()));
		values.add(new ColumnValue(DBConstants.COL_NAME_MATCH_MINUTE, goal.getMatchMinute()));
		values.add(new ColumnValue(DBConstants.COL_NAME_IS_OVERTIME, goal.isOvertime()));
		values.add(new ColumnValue(DBConstants.COL_NAME_IS_OWN_GOAL, goal.isOwnGoal()));
		values.add(new ColumnValue(DBConstants.COL_NAME_IS_PENALTY, goal.isPenalty()));

		mergedRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_GOAL, keyColumns, values.toArray(new ColumnValue[0]));

		if (mergedRows == 1) {
			LOGGER.info("The goal of {} in the match with id '{}' has been saved.",
			            goal.getGoalGetterName(),
			            goal.getMatchID());
			return true;
		}

		return false;
	}


	@Override
	public Goal findByID(JdbcTemplate jdbcTemplate, Integer id) {
		throw new UnsupportedOperationException("Fetching a single goal by ID is not supported.");
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, Integer id) {
		throw new UnsupportedOperationException("Deleting a goal is not supported.");
	}


	Goal[] getGoalsForMatchID(JdbcTemplate jdbcTemplate, Integer matchID) {
		final List<Goal>    resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_GOAL)
		            .append(WHERE).append(DBConstants.COL_NAME_MATCH_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { matchID }, (rs, rowNum) ->
				new Goal(rs.getBoolean(DBConstants.COL_NAME_IS_OVERTIME),
				         rs.getBoolean(DBConstants.COL_NAME_IS_OWN_GOAL),
				         rs.getBoolean(DBConstants.COL_NAME_IS_PENALTY),
				         rs.getInt(DBConstants.COL_NAME_GOAL_GETTER_ID),
				         rs.getInt(DBConstants.COL_NAME_MATCH_ID),
				         rs.getInt(DBConstants.COL_NAME_MATCH_MINUTE),
				         rs.getInt(DBConstants.COL_NAME_SCORE_TEAM_HOME),
				         rs.getInt(DBConstants.COL_NAME_SCORE_TEAM_GUEST),
				         rs.getString(DBConstants.COL_NAME_GOAL_GETTER_NAME)));

		return resultList.toArray(new Goal[0]);
	}
}

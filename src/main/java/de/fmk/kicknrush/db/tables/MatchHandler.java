package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Team;
import de.fmk.kicknrush.utils.TimeUtils;
import org.h2.api.TimestampWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
public class MatchHandler extends AbstractDBHandler<Integer, Match> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchHandler.class);

	private final GoalHandler   goalHandler;
	private final GroupHandler  groupHandler;
	private final TeamHandler   teamHandler;
	private final UpdateHandler updateHandler;


	public MatchHandler(GoalHandler   goalHandler,
	                    GroupHandler  groupHandler,
	                    TeamHandler   teamHandler,
	                    UpdateHandler updateHandler) {
		super();

		this.goalHandler   = goalHandler;
		this.groupHandler  = groupHandler;
		this.teamHandler   = teamHandler;
		this.updateHandler = updateHandler;
	}


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
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


	@Override
	public List<Integer> getIDs(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_MATCH_ID)
		            .append(" FROM ").append(DBConstants.TBL_NAME_MATCH);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> rs.getInt(DBConstants.COL_NAME_MATCH_ID));
	}


	@Override
	public List<Match> getValues(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_MATCH);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> {
			final LocalDateTime kickOff;
			final Match         match;

			kickOff = TimeUtils.convertTimestamp((TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_KICKOFF));
			match   = new Match(rs.getBoolean(DBConstants.COL_NAME_MATCH_OVER),
			                    rs.getInt(DBConstants.COL_NAME_MATCH_ID),
			                    goalHandler.getGoalsForMatchID(jdbcTemplate, rs.getInt(DBConstants.COL_NAME_MATCH_ID)),
			                    new Group(rs.getInt(DBConstants.COL_NAME_GROUP_ID)),
			                    TimeUtils.convertLocalDateTimeUTC(kickOff),
			                    new Team(rs.getInt(DBConstants.COL_NAME_TEAM_HOME)),
			                    new Team(rs.getInt(DBConstants.COL_NAME_TEAM_GUEST)));

			System.out.println(match.getTeam1().getTeamId() + " : " + match.getTeam2().getTeamId()
					+ " >> " + match.getMatchDateTimeUTC());

			return match;
		});
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Match match) {
		final int               mergedRows;
		final List<ColumnValue> values;
		final String[]          keyColumns;

		if (match == null) {
			LOGGER.warn("MERGE FAILED: The match parameter is null.");
			return false;
		}

		keyColumns = new String[] { DBConstants.COL_NAME_MATCH_ID };
		values     = new ArrayList<>();

		values.add(new ColumnValue(DBConstants.COL_NAME_MATCH_ID, match.getMatchID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_GROUP_ID, match.getGroup().getGroupID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_HOME, match.getTeam1().getTeamId()));
		values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_GUEST, match.getTeam2().getTeamId()));
		values.add(new ColumnValue(DBConstants.COL_NAME_MATCH_OVER, match.isMatchIsFinished()));

		if (match.getMatchDateTimeUTC() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_KICKOFF,
			                           TimeUtils.createTimestamp(match.getMatchDateTimeUTC(), true)));

		mergedRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_MATCH, keyColumns, values.toArray(new ColumnValue[0]));

		if (mergedRows == 1) {
			LOGGER.info("The match with id '{}' has been updated.", match.getMatchID());
			updateHandler.storeUpdate(jdbcTemplate, DBConstants.TBL_NAME_MATCH);
			return true;
		}

		return false;
	}


	@Override
	public Match findByID(JdbcTemplate jdbcTemplate, Integer id) {
		final List<Match>   resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_MATCH)
		            .append(WHERE).append(DBConstants.COL_NAME_MATCH_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { id }, (rs, rowNum) -> {
			final LocalDateTime kickOff;
			final Match         match;

			kickOff = TimeUtils.convertTimestamp((TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_KICKOFF));
			match   = new Match(rs.getBoolean(DBConstants.COL_NAME_MATCH_OVER),
			                    rs.getInt(DBConstants.COL_NAME_MATCH_ID),
			                    goalHandler.getGoalsForMatchID(jdbcTemplate, id),
			                    groupHandler.findByID(jdbcTemplate, rs.getInt(DBConstants.COL_NAME_GROUP_ID)),
			                    TimeUtils.convertLocalDateTimeUTC(kickOff),
			                    teamHandler.findByID(jdbcTemplate, rs.getInt(DBConstants.COL_NAME_TEAM_HOME)),
			                    teamHandler.findByID(jdbcTemplate, rs.getInt(DBConstants.COL_NAME_TEAM_GUEST)));

			return match;
		});

		return resultList.isEmpty() ? null : resultList.get(0);
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, Integer id) {
		throw new UnsupportedOperationException("Deleting a match is not supported.");
	}
}

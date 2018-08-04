package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
public class TeamHandler extends AbstractDBHandler<Integer, Team> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TeamHandler.class);

	private final UpdateHandler updateHandler;


	public TeamHandler(UpdateHandler updateHandler) {
		super();

		this.updateHandler = updateHandler;
	}


	@Override
	public void createTable(final JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the teams table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_TEAM)
		            .append("(").append(DBConstants.COL_NAME_TEAM_ID).append(" INTEGER PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_TEAM_ICON).append(" VARCHAR(255) NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_ICON_SMALL).append(" VARCHAR(255) NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_NAME).append(" VARCHAR(255) NOT NULL);");

		jdbcTemplate.execute(queryBuilder.toString());

		updateHandler.storeUpdate(jdbcTemplate, DBConstants.TBL_NAME_TEAM);
	}


	@Override
	public List<Integer> getIDs(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_TEAM_ID)
		            .append(" FROM ").append(DBConstants.TBL_NAME_TEAM);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> rs.getInt(DBConstants.COL_NAME_TEAM_ID));
	}


	@Override
	public List<Team> getValues(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_TEAM);

		return jdbcTemplate.query(queryBuilder.toString(),
		                         (rs, rowNum) -> new Team(rs.getInt(DBConstants.COL_NAME_TEAM_ID),
		                                                  rs.getString(DBConstants.COL_NAME_TEAM_ICON),
		                                                  rs.getString(DBConstants.COL_NAME_TEAM_ICON_SMALL),
		                                                  rs.getString(DBConstants.COL_NAME_TEAM_NAME)));
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Team team) {
		final int               mergedRows;
		final List<ColumnValue> values;
		final String[]          keyColumns;

		if (team == null) {
			LOGGER.warn("MERGE FAILED: The team parameter is null.");
			return false;
		}

		keyColumns = new String[] { DBConstants.COL_NAME_TEAM_ID };
		values     = new ArrayList<>();

		values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_ID, team.getTeamId()));

		if (team.getTeamName() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_NAME, team.getTeamName()));
		if (team.getTeamIconUrl() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_ICON, team.getTeamIconUrl()));
		if (team.getTeamIconUrlSmall() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_TEAM_ICON_SMALL, team.getTeamIconUrlSmall()));

		mergedRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_TEAM, keyColumns, values.toArray(new ColumnValue[0]));

		if (mergedRows == 1) {
			LOGGER.info("The team with id '{}' and name '{}' has been updated.", team.getTeamId(), team.getTeamName());
			updateHandler.storeUpdate(jdbcTemplate, DBConstants.TBL_NAME_TEAM);
			return true;
		}

		return false;
	}


	@Override
	public Team findByID(JdbcTemplate jdbcTemplate, Integer id) {
		final List<Team>    resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_TEAM)
		            .append(WHERE).append(DBConstants.COL_NAME_TEAM_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { id }, (rs, rowNum) ->
				new Team(rs.getInt(DBConstants.COL_NAME_TEAM_ID),
				         rs.getString(DBConstants.COL_NAME_TEAM_ICON),
				         rs.getString(DBConstants.COL_NAME_TEAM_ICON_SMALL),
				         rs.getString(DBConstants.COL_NAME_TEAM_NAME)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, Integer id) {
		throw new UnsupportedOperationException("Deleting a team is currently not supported.");
	}
}

package de.fmk.kicknrush.db;

import de.fmk.kicknrush.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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
	public boolean merge(JdbcTemplate jdbcTemplate, Team value) {
		final int      createdRows;
		final Object[] values;
		final String[] keyColumns;

		keyColumns = new String[] { DBConstants.COL_NAME_TEAM_ID };
		values     = new Object[] { value.getTeamId(),
		                            value.getTeamIconUrl(),
		                            value.getTeamIconUrlSmall() == null ? value.getTeamIconUrl()
		                                                                : value.getTeamIconUrlSmall(),
		                            value.getTeamName() };
		createdRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_TEAM, keyColumns, values);

		if (createdRows == 1) {
			LOGGER.info("The team with id '{}' and name '{}' has been updated.", value.getTeamId(), value.getTeamName());
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
}

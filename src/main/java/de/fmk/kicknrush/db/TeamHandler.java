package de.fmk.kicknrush.db;

import de.fmk.kicknrush.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;


/**
 * @author FabianK
 */
public class TeamHandler extends AbstractDBHandler<Integer, Team> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TeamHandler.class);


	@Override
	public void createTable(final JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the teams table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_TEAM)
		            .append("(").append(DBConstants.COL_NAME_TEAM_ID).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_YEAR).append(" INTEGER NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_ICON).append(" VARCHAR(255) NOT NULL, ")
		            .append(DBConstants.COL_NAME_TEAM_NAME).append(" VARCHAR(255) NOT NULL, ")
		            .append("PRIMARY KEY(").append(DBConstants.COL_NAME_TEAM_ID).append(", ")
		            .append(DBConstants.COL_NAME_YEAR).append("));");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	@Override
	public List<Integer> getIDs(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_TEAM_ID)
		            .append(" FROM ").append(DBConstants.TBL_NAME_TEAM)
		            .append(WHERE).append(DBConstants.COL_NAME_YEAR).append("=?;");

		return jdbcTemplate.query(queryBuilder.toString(),
		                          new Object[] { LocalDate.now().getYear() },
		                          (rs, rowNum) -> rs.getInt(DBConstants.COL_NAME_TEAM_ID));
	}


	@Override
	public List<Team> getValues(JdbcTemplate jdbcTemplate) {
		return getValuesOfYear(jdbcTemplate, LocalDate.now().getYear());
	}


	public List<Team> getValuesOfYear(JdbcTemplate jdbcTemplate, int year) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_TEAM)
		            .append(WHERE).append(DBConstants.COL_NAME_YEAR).append("=?;");

		return jdbcTemplate.query(queryBuilder.toString(),
		                          new Object[] { year },
		                         (rs, rowNum) -> new Team(rs.getInt(DBConstants.COL_NAME_TEAM_ID),
		                                                  rs.getString(DBConstants.COL_NAME_TEAM_ID),
		                                                  rs.getString(DBConstants.COL_NAME_TEAM_NAME)));
	}


	@Override
	public boolean add(JdbcTemplate jdbcTemplate, Team value) {
		final int      createdRows;
		final Object[] values;
		final String[] columnNames;

		columnNames = new String[]{ DBConstants.COL_NAME_TEAM_ID,
		                            DBConstants.COL_NAME_YEAR,
		                            DBConstants.COL_NAME_TEAM_ICON,
		                            DBConstants.COL_NAME_TEAM_NAME };
		values      = new Object[] { value.getTeamId(),
		                             LocalDate.now().getYear(),
		                             value.getTeamIconUrl(),
		                             value.getTeamName() };
		createdRows = insertInto(jdbcTemplate, DBConstants.TBL_NAME_TEAM, columnNames, values);

		if (createdRows == 1) {
			LOGGER.info("The team with id '{}' and name '{}' has been created.", value.getTeamId(), value.getTeamName());
			return true;
		}

		return false;
	}


	@Override
	public Team findByID(JdbcTemplate jdbcTemplate, Integer id) {
		final int           year;
		final List<Team>    resultList;
		final StringBuilder queryBuilder;

		year         = LocalDate.now().getYear();
		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_TEAM)
		            .append(WHERE).append(DBConstants.COL_NAME_TEAM_ID).append("=? AND ")
		            .append(DBConstants.COL_NAME_YEAR).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[]{ id, year }, (rs, rowNum) ->
				new Team(rs.getInt(DBConstants.COL_NAME_TEAM_ID),
				         rs.getString(DBConstants.COL_NAME_TEAM_ID),
				         rs.getString(DBConstants.COL_NAME_TEAM_NAME)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

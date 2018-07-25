package de.fmk.kicknrush.db;

import de.fmk.kicknrush.models.Update;
import de.fmk.kicknrush.utils.TimeUtils;
import org.h2.api.TimestampWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @author FabianK
 */
public class UpdateHandler extends AbstractDBHandler<String, Update> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the updates table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_UPDATES)
		            .append("(").append(DBConstants.COL_NAME_TABLE_NAME).append(" VARCHAR(255) PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_LAST_UPDATE).append(" TIMESTAMP WITH TIME ZONE NOT NULL);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	@Override
	public List<String> getIDs(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching the IDs only is not supported.");
	}


	@Override
	public List<Update> getValues(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_UPDATES);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> {
			final LocalDateTime         time;
			final String                timeString;
			final TimestampWithTimeZone timestamp;

			timestamp  = (TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_LAST_UPDATE);
			time       = TimeUtils.convertTimestamp(timestamp);
			timeString = TimeUtils.convertLocalDateTimeUTC(time);

			return new Update(timeString, rs.getString(DBConstants.COL_NAME_TABLE_NAME));
		});
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Update value) {
		final int      mergedRows;
		final Object[] values;
		final String[] keyColumns;

		keyColumns = new String[] { DBConstants.COL_NAME_TABLE_NAME };
		values     = new Object[] { value.getTableName(), TimeUtils.createTimestamp(value.getLastUpdateUTC()) };
		mergedRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_UPDATES, keyColumns, values);

		if (mergedRows == 1) {
			LOGGER.info("The update of table {} has been stored in the updates table.", value.getTableName());
			return true;
		}

		return false;
	}


	@Override
	public Update findByID(JdbcTemplate jdbcTemplate, String id) {
		final List<Update>  resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_UPDATES)
		            .append(WHERE).append(DBConstants.COL_NAME_TABLE_NAME).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { id }, (rs, rowNum) -> {
			final LocalDateTime         time;
			final String                timeString;
			final TimestampWithTimeZone timestamp;

			timestamp  = (TimestampWithTimeZone) rs.getObject(DBConstants.COL_NAME_LAST_UPDATE);
			time       = TimeUtils.convertTimestamp(timestamp);
			timeString = TimeUtils.convertLocalDateTimeUTC(time);

			return new Update(timeString, rs.getString(DBConstants.COL_NAME_TABLE_NAME));
		});

		return resultList.isEmpty() ? null : resultList.get(0);
	}


	public void storeUpdate(final JdbcTemplate jdbcTemplate, final String tableName) {
		final LocalDateTime now;

		now = LocalDateTime.now(Clock.systemUTC());

		merge(jdbcTemplate, new Update(TimeUtils.convertLocalDateTimeUTC(now), tableName));
	}
}

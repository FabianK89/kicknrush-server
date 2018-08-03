package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.Session;
import de.fmk.kicknrush.utils.TimeUtils;
import org.h2.api.TimestampWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;


/**
 * @author FabianK
 */
public class SessionHandler extends AbstractDBHandler<UUID, Session> {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);


	public SessionHandler() {
		super();
	}


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the sessions table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_SESSION)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USER_ID).append(" UUID UNIQUE, ")
		            .append(DBConstants.COL_NAME_LOGGED_IN).append(" TIMESTAMP WITH TIME ZONE, ")
		            .append(DBConstants.COL_NAME_LAST_ACTION).append(" TIMESTAMP WITH TIME ZONE);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	@Override
	public List<UUID> getIDs(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching the IDs only is not supported.");
	}


	@Override
	public List<Session> getValues(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching all sessions is not supported.");
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Session value) {
		final int                   createdRows;
		final Object[]              values;
		final String[]              keyColumns;

		keyColumns = new String[] { DBConstants.COL_NAME_ID };
		values     = new Object[] { value.getSessionID(),
		                            value.getUserID(),
		                            TimeUtils.createTimestamp(value.getLoggedInTime()),
		                            TimeUtils.createTimestamp(value.getLastActionTime()) };
		createdRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_SESSION, keyColumns, values);

		if (createdRows == 1) {
			LOGGER.info("Session has been updated for the user with id '{}'.", value.getUserID());
			return true;
		}

		return false;
	}


	@Override
	public Session findByID(JdbcTemplate jdbcTemplate, UUID id) {
		final List<Session> resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_SESSION)
		            .append(WHERE).append(DBConstants.COL_NAME_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { id }, (rs, rowNum) -> {
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

		return resultList.isEmpty() ? null : resultList.get(0);
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, UUID id) {
		return deleteByID(jdbcTemplate, DBConstants.TBL_NAME_SESSION, DBConstants.COL_NAME_ID, id) == 1;
	}
}

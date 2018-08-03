package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author FabianK
 */
public class UserHandler extends AbstractDBHandler<UUID, User> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

	private final UpdateHandler updateHandler;


	public UserHandler(UpdateHandler updateHandler) {
		super();

		this.updateHandler = updateHandler;
	}


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		LOGGER.info("Create the user table.");

		queryBuilder = new StringBuilder();
		queryBuilder.append(CREATE_TABLE_IF_NOT_EXISTS)
		            .append(DBConstants.TBL_NAME_USER)
		            .append("(").append(DBConstants.COL_NAME_ID).append(" UUID PRIMARY KEY, ")
		            .append(DBConstants.COL_NAME_USERNAME).append(" VARCHAR(255) UNIQUE, ")
		            .append(DBConstants.COL_NAME_PWD).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_SALT).append(" VARCHAR(255), ")
		            .append(DBConstants.COL_NAME_IS_ADMIN).append(" BOOLEAN);");

		jdbcTemplate.execute(queryBuilder.toString());
	}


	@Override
	public List<UUID> getIDs(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching the IDs only is not supported.");
	}


	@Override
	public List<User> getValues(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_USER);

		return jdbcTemplate.query(queryBuilder.toString(),
				(rs, rowNum) -> new User(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN),
				                         null,
				                         rs.getString(DBConstants.COL_NAME_USERNAME),
				                         (UUID) rs.getObject(DBConstants.COL_NAME_ID)));
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, User value) {
		final int               createdRows;
		final List<ColumnValue> values;
		final String[]          keyColumns;

		if (value == null) {
			LOGGER.warn("MERGE FAILED: The user parameter is null.");
			return false;
		}

		keyColumns = new String[] { DBConstants.COL_NAME_ID };
		values     = new ArrayList<>();

		values.add(new ColumnValue(DBConstants.COL_NAME_ID, value.getId()));
		values.add(new ColumnValue(DBConstants.COL_NAME_IS_ADMIN, value.isAdmin()));

		if (value.getUsername() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_USERNAME, value.getUsername()));
		if (value.getPassword() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_PWD, value.getPassword()));
		if (value.getSalt() != null)
			values.add(new ColumnValue(DBConstants.COL_NAME_SALT, value.getSalt()));

		createdRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_USER, keyColumns, values.toArray(new ColumnValue[0]));

		if (createdRows == 1) {
			LOGGER.info("The user with name '{}' has been updated.", value.getUsername());
			updateHandler.storeUpdate(jdbcTemplate, DBConstants.TBL_NAME_USER);
			return true;
		}

		return false;
	}


	@Override
	public User findByID(JdbcTemplate jdbcTemplate, UUID id) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_USER)
		            .append(WHERE).append(DBConstants.COL_NAME_ID).append("=?;");

		return find(jdbcTemplate, queryBuilder.toString(), id);
	}


	public User findByUsername(JdbcTemplate jdbcTemplate, String username) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_USER)
		            .append(WHERE).append(DBConstants.COL_NAME_USERNAME).append("=?;");

		return find(jdbcTemplate, queryBuilder.toString(), username);
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, UUID id) {
		return deleteByID(jdbcTemplate, DBConstants.TBL_NAME_USER, DBConstants.COL_NAME_ID, id) == 1;
	}


	public List<String> getUsernames(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT ").append(DBConstants.COL_NAME_USERNAME)
		            .append(" FROM ").append(DBConstants.TBL_NAME_USER);

		return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> rs.getString(DBConstants.COL_NAME_USERNAME));
	}


	private User find(JdbcTemplate jdbcTemplate, String query, Object searchObject) {
		final List<User> resultList;

		resultList = jdbcTemplate.query(query, new Object[] { searchObject }, (rs, rowNum) ->
				new User(rs.getBoolean(DBConstants.COL_NAME_IS_ADMIN),
				         rs.getString(DBConstants.COL_NAME_PWD),
				         rs.getString(DBConstants.COL_NAME_SALT),
				         rs.getString(DBConstants.COL_NAME_USERNAME),
				         (UUID) rs.getObject(DBConstants.COL_NAME_ID),
				         null));

		return resultList.isEmpty() ? null : resultList.get(0);
	}
}

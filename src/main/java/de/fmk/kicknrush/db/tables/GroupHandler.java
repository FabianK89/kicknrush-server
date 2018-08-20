package de.fmk.kicknrush.db.tables;

import de.fmk.kicknrush.db.ColumnValue;
import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.models.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
public class GroupHandler extends AbstractDBHandler<Integer, Group> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GroupHandler.class);

	private final UpdateHandler updateHandler;


	public GroupHandler(UpdateHandler updateHandler) {
		super();

		this.updateHandler = updateHandler;
	}


	@Override
	public void createTable(JdbcTemplate jdbcTemplate) {
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


	@Override
	public List<Integer> getIDs(JdbcTemplate jdbcTemplate) {
		throw new UnsupportedOperationException("Fetching the IDs only is not supported.");
	}


	@Override
	public List<Group> getValues(JdbcTemplate jdbcTemplate) {
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_GROUP);

		return jdbcTemplate.query(queryBuilder.toString(),
				(rs, rowNum) -> new Group(rs.getInt(DBConstants.COL_NAME_GROUP_ID),
				                          rs.getInt(DBConstants.COL_NAME_GROUP_ORDER_ID),
				                          rs.getInt(DBConstants.COL_NAME_YEAR),
				                          rs.getString(DBConstants.COL_NAME_GROUP_NAME)));
	}


	@Override
	public boolean merge(JdbcTemplate jdbcTemplate, Group group) {
		final int               mergedRows;
		final List<ColumnValue> values;
		final String[]          keyColumns;

		keyColumns = new String[] { DBConstants.COL_NAME_GROUP_ID };
		values     = new ArrayList<>();

		values.add(new ColumnValue(DBConstants.COL_NAME_GROUP_ID, group.getGroupID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_GROUP_NAME, group.getGroupName()));
		values.add(new ColumnValue(DBConstants.COL_NAME_GROUP_ORDER_ID, group.getGroupOrderID()));
		values.add(new ColumnValue(DBConstants.COL_NAME_YEAR, group.getYear()));

		mergedRows = mergeInto(jdbcTemplate, DBConstants.TBL_NAME_GROUP, keyColumns, values.toArray(new ColumnValue[0]));

		if (mergedRows == 1) {
			LOGGER.info("The group with id '{}' and name '{}' has been updated.", group.getGroupID(), group.getGroupName());
			updateHandler.storeUpdate(jdbcTemplate, DBConstants.TBL_NAME_GROUP);
			return true;
		}

		return false;
	}


	@Override
	public Group findByID(JdbcTemplate jdbcTemplate, Integer id) {
		final List<Group>   resultList;
		final StringBuilder queryBuilder;

		queryBuilder = new StringBuilder();
		queryBuilder.append(SELECT_ALL_FROM).append(DBConstants.TBL_NAME_GROUP)
		            .append(WHERE).append(DBConstants.COL_NAME_GROUP_ID).append("=?;");

		resultList = jdbcTemplate.query(queryBuilder.toString(), new Object[] { id },
				(rs, rowNum) -> new Group(rs.getInt(DBConstants.COL_NAME_GROUP_ID),
				                          rs.getInt(DBConstants.COL_NAME_GROUP_ORDER_ID),
				                          rs.getInt(DBConstants.COL_NAME_YEAR),
				                          rs.getString(DBConstants.COL_NAME_GROUP_NAME)));

		return resultList.isEmpty() ? null : resultList.get(0);
	}


	@Override
	public boolean deleteByID(JdbcTemplate jdbcTemplate, Integer id) {
		throw new UnsupportedOperationException("Deleting a group is not supported.");
	}
}

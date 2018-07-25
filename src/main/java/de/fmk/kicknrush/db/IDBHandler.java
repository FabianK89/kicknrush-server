package de.fmk.kicknrush.db;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;


/**
 * @author FabianK
 */
public interface IDBHandler<K, V> {
	void createTable(final JdbcTemplate jdbcTemplate);

	List<K> getIDs(final JdbcTemplate jdbcTemplate);

	List<V> getValues(final JdbcTemplate jdbcTemplate);

	boolean merge(final JdbcTemplate jdbcTemplate, final V value);

	V findByID(final JdbcTemplate jdbcTemplate, K id);
}

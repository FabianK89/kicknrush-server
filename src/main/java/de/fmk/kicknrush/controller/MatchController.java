package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DBConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/match")
public class MatchController {
	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RequestMapping("/admin/dropTables")
	public void dropTables() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_GROUP);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_MATCH);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_TEAM);
	}
}

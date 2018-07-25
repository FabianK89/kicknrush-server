package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.Team;
import de.fmk.kicknrush.service.OpenLigaDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path="/api/liga")
public class LigaController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LigaController.class);

	@Autowired
	private DatabaseHandler   dbHandler;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private OpenLigaDBService oldbService;


	@RequestMapping("/getTeams")
	public List<Team> getTeams() {
		List<Team> teams;

		teams = dbHandler.getTeams();

		if (teams.isEmpty()) {
			teams = oldbService.getTeams();
			teams.forEach(team -> dbHandler.addTeam(team));
		}

		if (teams.isEmpty())
			teams = dbHandler.getTeamsOfLastYear();

		return teams;
	}


	@RequestMapping("/admin/dropTables")
	public void dropTables() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_GROUP);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_MATCH);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_TEAM);
		LOGGER.info("Tables have been dropped.");
	}
}

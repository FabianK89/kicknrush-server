package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.dto.MatchDTO;
import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Team;
import de.fmk.kicknrush.service.OpenLigaDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/**
 * @author FabianK
 */
@RestController
@RequestMapping(path="/api/league")
public class LeagueController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LeagueController.class);

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
			teams.forEach(team -> {
				team.setTeamIconUrlSmall(team.getTeamIconUrl());
				dbHandler.addTeam(team);
			});
		}

		return teams;
	}


	@RequestMapping("/loadGroups")
	public boolean loadGroups() {
		List<Group> groups = oldbService.getGroups();

		groups.forEach(group -> dbHandler.addGroup(group));

		return !groups.isEmpty();
	}


	@RequestMapping("/loadMatches")
	public boolean loadMatches() {
		List<Match> matches = oldbService.getAllMatches();

		matches.forEach(match -> dbHandler.addMatch(match));

		return !matches.isEmpty();
	}


	/**
	 * Collect all matches from the databae.
	 * @param sessionID ID of an user session.
	 * @param userID ID of the user belonging to the session.
	 * @return an array with all matches.
	 */
	@PostMapping("/getMatches")
	public ResponseEntity<MatchDTO[]> getMatches(@RequestParam String sessionID, @RequestParam String userID) {
		final List<MatchDTO>             matches;
//		final ResponseEntity<MatchDTO[]> sessionResponse;
//
//		sessionResponse = SessionHelper.isValidUserSession(dbHandler, sessionID, userID);
//
//		if (sessionResponse.getStatusCode() != HttpStatus.OK)
//			return sessionResponse;

		matches = new ArrayList<>();

		dbHandler.getMatches().forEach(match -> matches.add(match.toDTO()));

		return ResponseEntity.ok(matches.toArray(new MatchDTO[0]));
	}


	@RequestMapping("/admin/dropTables")
	public void dropTables() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_GROUP);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_MATCH);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_TEAM);
		jdbcTemplate.execute("DROP TABLE IF EXISTS " + DBConstants.TBL_NAME_GOAL);
		LOGGER.info("Tables have been dropped.");
	}
}

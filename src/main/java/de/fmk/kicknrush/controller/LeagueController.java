package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DBConstants;
import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.dto.GroupDTO;
import de.fmk.kicknrush.dto.MatchDTO;
import de.fmk.kicknrush.dto.TeamDTO;
import de.fmk.kicknrush.dto.UserDTO;
import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Team;
import de.fmk.kicknrush.service.OpenLigaDBService;
import de.fmk.kicknrush.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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


	@RequestMapping("/load")
	public boolean load() {
		return loadTeams() && loadGroups() && loadMatches();
	}


	@RequestMapping("/loadTeams")
	public boolean loadTeams() {
		final List<Team> teams;
		final String     homeDir;

		teams   = oldbService.getTeams();
		homeDir = System.getProperty("user.home");

		teams.forEach(team -> {
			final String url = team.getTeamIconUrl();

			if (url != null) {
				try {
					ImageUtils.storeTeamLogo(homeDir, url, team.getTeamName(), false).ifPresent(team::setTeamIconUrl);
					ImageUtils.storeTeamLogo(homeDir, url, team.getTeamName(), true).ifPresent(team::setTeamIconUrlSmall);
				}
				catch (IOException ioex) {
					LOGGER.error("Could not store the logo for the team {}.", team.getTeamName(), ioex);
				}
			}

			dbHandler.addTeam(team);
		});

		return !teams.isEmpty();
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
	 * Collect all groups from the database.
	 * @param body Request body: Session id and user id must be set.
	 * @return an array with all groups.
	 */
	@PostMapping("/getGroups")
	public ResponseEntity<GroupDTO[]> getGroups(@RequestBody UserDTO body) {
		final List<GroupDTO>             groups;
		final ResponseEntity<GroupDTO[]> sessionResponse;

		sessionResponse = SessionHelper.isValidUserSession(dbHandler, body.getSessionID(), body.getUserID());

		if (sessionResponse.getStatusCode() != HttpStatus.OK)
			return sessionResponse;

		groups = new ArrayList<>();

		dbHandler.getGroups().forEach(group -> groups.add(group.toDTO()));

		return ResponseEntity.ok(groups.toArray(new GroupDTO[0]));
	}


	/**
	 * Collect all matches from the database.
	 * @param body Request body: Session id and user id must be set.
	 * @return an array with all matches.
	 */
	@PostMapping("/getMatches")
	public ResponseEntity<MatchDTO[]> getMatches(@RequestBody UserDTO body) {
		final List<MatchDTO>             matches;
		final ResponseEntity<MatchDTO[]> sessionResponse;

		sessionResponse = SessionHelper.isValidUserSession(dbHandler, body.getSessionID(), body.getUserID());

		if (sessionResponse.getStatusCode() != HttpStatus.OK)
			return sessionResponse;

		matches = new ArrayList<>();

		dbHandler.getMatches().forEach(match -> matches.add(match.toDTO()));

		return ResponseEntity.ok(matches.toArray(new MatchDTO[0]));
	}


	/**
	 * Collect all teams from the database.
	 * @param body Request body: Session id and user id must be set.
	 * @return an array with all teams.
	 */
	@PostMapping("/getTeams")
	public ResponseEntity<TeamDTO[]> getTeams(@RequestBody UserDTO body) {
		final List<TeamDTO>             teams;
		final ResponseEntity<TeamDTO[]> sessionResponse;

		sessionResponse = SessionHelper.isValidUserSession(dbHandler, body.getSessionID(), body.getUserID());

		if (sessionResponse.getStatusCode() != HttpStatus.OK)
			return sessionResponse;

		teams = new ArrayList<>();

		dbHandler.getTeams().forEach(team -> teams.add(team.toDTO()));

		return ResponseEntity.ok(teams.toArray(new TeamDTO[0]));
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

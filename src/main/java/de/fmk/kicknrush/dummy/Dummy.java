package de.fmk.kicknrush.dummy;

import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.Team;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.UUID;


/**
 * @author FabianK
 */
public class Dummy {
	public static void createTeams(final DatabaseHandler dbHandler) {
		final List<Integer> teamIDs;
		final List<Team>    teams;

		teamIDs = dbHandler.getTeamIDs();
		teams = FXCollections.observableArrayList(new Team(79, "1. FC Nürnberg", UUID.randomUUID()),
		                                          new Team(81, "1. FSV Mainz 05", UUID.randomUUID()),
		                                          new Team(6, "Bayer Leverkusen", UUID.randomUUID()),
		                                          new Team(7, "Borussia Dortmund", UUID.randomUUID()),
		                                          new Team(87, "Borussia Mönchengladbach", UUID.randomUUID()),
		                                          new Team(91, "Eintracht Frankfurt", UUID.randomUUID()),
		                                          new Team(95, "FC Augsburg", UUID.randomUUID()),
		                                          new Team(40, "FC Bayern", UUID.randomUUID()),
		                                          new Team(9, "FC Schalke 04", UUID.randomUUID()),
		                                          new Team(185, "Fortuna Düsseldorf", UUID.randomUUID()),
		                                          new Team(55, "Hannover 96", UUID.randomUUID()),
		                                          new Team(54, "Hertha BSC", UUID.randomUUID()),
		                                          new Team(1635, "RB Leipzig", UUID.randomUUID()),
		                                          new Team(112, "SC Freiburg", UUID.randomUUID()),
		                                          new Team(123, "TSG 1899 Hoffenheim", UUID.randomUUID()),
		                                          new Team(16, "VfB Stuttgart", UUID.randomUUID()),
		                                          new Team(131, "VfL Wolfsburg", UUID.randomUUID()),
		                                          new Team(134, "Werder Bremen", UUID.randomUUID()) );

		teams.forEach(team -> {
			if (!teamIDs.contains(team.getTeamId()))
				dbHandler.addTeam(team);
		});
	}
}

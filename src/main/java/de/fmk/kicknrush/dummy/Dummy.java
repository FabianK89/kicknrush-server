package de.fmk.kicknrush.dummy;

import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Team;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * @author FabianK
 */
public class Dummy {
	public static void createGroups(final DatabaseHandler dbHandler) {
		final List<Integer> groupIDs;
		final List<Group>   groups;

		groupIDs = dbHandler.getGroupIDsForYear(LocalDate.now().getYear());
		groups   = FXCollections.observableArrayList(
				new Group(31775, 1, 2018, "Spieltag 1"),
				new Group(31776, 2, 2018, "Spieltag 2"),
				new Group(31777, 3, 2018, "Spieltag 3"),
				new Group(31778, 4, 2018, "Spieltag 4"),
				new Group(31779, 5, 2018, "Spieltag 5"),
				new Group(31780, 6, 2018, "Spieltag 6"),
				new Group(31781, 7, 2018, "Spieltag 7"),
				new Group(31782, 8, 2018, "Spieltag 8"),
				new Group(31783, 9, 2018, "Spieltag 9"),
				new Group(31784, 10, 2018, "Spieltag 10"),
				new Group(31785, 11, 2018, "Spieltag 11"),
				new Group(31786, 12, 2018, "Spieltag 12"),
				new Group(31787, 13, 2018, "Spieltag 13"),
				new Group(31788, 14, 2018, "Spieltag 14"),
				new Group(31789, 15, 2018, "Spieltag 15"),
				new Group(31790, 16, 2018, "Spieltag 16"),
				new Group(31791, 17, 2018, "Spieltag 17"),
				new Group(31792, 18, 2018, "Spieltag 18"),
				new Group(31793, 19, 2018, "Spieltag 19"),
				new Group(31794, 20, 2018, "Spieltag 20"),
				new Group(31795, 21, 2018, "Spieltag 21"),
				new Group(31796, 22, 2018, "Spieltag 22"),
				new Group(31797, 23, 2018, "Spieltag 23"),
				new Group(31798, 24, 2018, "Spieltag 24"),
				new Group(31799, 25, 2018, "Spieltag 25"),
				new Group(31800, 26, 2018, "Spieltag 26"),
				new Group(31801, 27, 2018, "Spieltag 27"),
				new Group(31802, 28, 2018, "Spieltag 28"),
				new Group(31803, 29, 2018, "Spieltag 29"),
				new Group(31804, 30, 2018, "Spieltag 30"),
				new Group(31805, 31, 2018, "Spieltag 31"),
				new Group(31806, 32, 2018, "Spieltag 32"),
				new Group(31807, 33, 2018, "Spieltag 33"),
				new Group(31808, 34, 2018, "Spieltag 34")
		);

		groups.forEach(group -> {
			if (!groupIDs.contains(group.getGroupID()))
				dbHandler.addGroup(group);
		});
	}


	public static void createTeams(final DatabaseHandler dbHandler) {
		final List<Integer> teamIDs;
		final List<Team>    teams;

		teamIDs = dbHandler.getTeamIDs();
		teams = FXCollections.observableArrayList(
				new Team(79, null, "1. FC Nürnberg"),
				new Team(81, null, "1. FSV Mainz 05"),
				new Team(6, null, "Bayer Leverkusen"),
				new Team(7, null, "Borussia Dortmund"),
				new Team(87, null, "Borussia Mönchengladbach"),
				new Team(91, null, "Eintracht Frankfurt"),
				new Team(95, null, "FC Augsburg"),
				new Team(40, null, "FC Bayern"),
				new Team(9, null, "FC Schalke 04"),
				new Team(185, null, "Fortuna Düsseldorf"),
				new Team(55, null, "Hannover 96"),
				new Team(54, null, "Hertha BSC"),
				new Team(1635, null, "RB Leipzig"),
				new Team(112, null, "SC Freiburg"),
				new Team(123, null, "TSG 1899 Hoffenheim"),
				new Team(16, null, "VfB Stuttgart"),
				new Team(131, null, "VfL Wolfsburg"),
				new Team(134, null, "Werder Bremen")
		);

		teams.forEach(team -> {
			if (!teamIDs.contains(team.getTeamId()))
				dbHandler.addTeam(team);
		});
	}


	public static void createMatches(final DatabaseHandler dbHandler) {
		final DateTimeFormatter format;
		final List<Integer>     matchIDs;
		final List<Match>       matches;
		final ZonedDateTime     time;

		format   = DateTimeFormatter.ISO_INSTANT;
		time     = ZonedDateTime.now();
		matchIDs = dbHandler.getMatchIDs();
		matches  = FXCollections.observableArrayList(
				new Match(false, 51121, dbHandler.findGroup(31775), time.plusMinutes(1).format(format), dbHandler.findTeam(40), dbHandler.findTeam(123)),
				new Match(false, 51120, dbHandler.findGroup(31775), time.plusMinutes(2).format(format), dbHandler.findTeam(54), dbHandler.findTeam(79)),
				new Match(false, 51124, dbHandler.findGroup(31775), time.plusMinutes(3).format(format), dbHandler.findTeam(134), dbHandler.findTeam(55)),
				new Match(false, 51125, dbHandler.findGroup(31775), time.plusMinutes(4).format(format), dbHandler.findTeam(112), dbHandler.findTeam(91)),
				new Match(false, 51127, dbHandler.findGroup(31775), time.plusMinutes(5).format(format), dbHandler.findTeam(131), dbHandler.findTeam(9)),
				new Match(false, 51128, dbHandler.findGroup(31775), time.plusMinutes(6).format(format), dbHandler.findTeam(185), dbHandler.findTeam(95)),
				new Match(false, 51123, dbHandler.findGroup(31775), time.plusMinutes(7).format(format), dbHandler.findTeam(87), dbHandler.findTeam(6)),
				new Match(false, 51126, dbHandler.findGroup(31775), time.plusMinutes(8).format(format), dbHandler.findTeam(81), dbHandler.findTeam(16)),
				new Match(false, 51122, dbHandler.findGroup(31775), time.plusMinutes(9).format(format), dbHandler.findTeam(7), dbHandler.findTeam(1635))
		);

		matches.forEach(match -> {
			if (!matchIDs.contains(match.getMatchID()))
				dbHandler.addMatch(match);
		});
	}
}

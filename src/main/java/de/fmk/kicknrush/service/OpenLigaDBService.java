package de.fmk.kicknrush.service;

import de.fmk.kicknrush.models.Group;
import de.fmk.kicknrush.models.Match;
import de.fmk.kicknrush.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author FabianK
 */
@Component
public class OpenLigaDBService {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenLigaDBService.class);
	private static final String URL    = "https://www.openligadb.de/api/";

	@Autowired
	private RestTemplate restTemplate;


	public List<Group> getGroups() {
		final Group[] groups;
		final String  restUrl;

		restUrl = URL.concat("getavailablegroups/bl1/").concat(Integer.toString(LocalDate.now().getYear()));

		LOGGER.info("Fetch groups from {}.", restUrl);

		try {
			groups = restTemplate.getForObject(restUrl, Group[].class);
		}
		catch (HttpServerErrorException hseex) {
			LOGGER.error("Could not fetch groups from {}.", restUrl, hseex);
			return new ArrayList<>();
		}

		return Arrays.asList(groups);
	}


	public List<Match> getAllMatches() {
		final Match[] matches;
		final String  restUrl;

		restUrl = URL.concat("getmatchdata/bl1/").concat(Integer.toString(LocalDate.now().getYear()));

		LOGGER.info("Fetch all matches from {}.", restUrl);

		try {
			matches = restTemplate.getForObject(restUrl, Match[].class);
		}
		catch (HttpServerErrorException hseex) {
			LOGGER.error("Could not fetch matches from {}.", restUrl, hseex);
			return new ArrayList<>();
		}

		return Arrays.asList(matches);
	}


	/**
	 * Get the teams of '1. Bundesliga' in the current year.
	 * @return a list of the teams or en empty list if an error occurred.
	 */
	public List<Team> getTeams() {
		final String restUrl;
		final Team[] teams;

		restUrl = URL.concat("getavailableteams/bl1/").concat(Integer.toString(LocalDate.now().getYear()));

		LOGGER.info("Fetch teams from {}.", restUrl);

		try {
			teams = restTemplate.getForObject(restUrl, Team[].class);
		}
		catch (HttpServerErrorException hseex)
		{
			LOGGER.error("Could not fetch teams from {}.", restUrl, hseex);
			return new ArrayList<>();
		}

		return Arrays.asList(teams);
	}
}

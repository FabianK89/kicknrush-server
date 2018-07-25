package de.fmk.kicknrush.service;

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
			LOGGER.info("Could not fetch teams from {}.", restUrl);
			return new ArrayList<>();
		}

		return Arrays.asList(teams);
	}
}

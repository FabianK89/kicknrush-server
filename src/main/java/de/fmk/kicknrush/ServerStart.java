package de.fmk.kicknrush;

import de.fmk.kicknrush.db.DatabaseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@ComponentScan(basePackages = "de.fmk.kicknrush")
public class ServerStart implements CommandLineRunner {
	@Autowired
	private DatabaseHandler dbHandler;
	@Autowired
	private JdbcTemplate jdbcTemplate;


	public static void main(String[] args) {
		SpringApplication.run(ServerStart.class, args);
	}


	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}


	@Override
	public void run(String... args) throws Exception {
//		final DatabaseHandler dbHandler;

//		dbHandler = new DatabaseHandler(jdbcTemplate);
		dbHandler.createInitialTables();

//		Dummy.createGroups(dbHandler);
//		Dummy.createTeams(dbHandler);
//		Dummy.createMatches(dbHandler);
	}
}

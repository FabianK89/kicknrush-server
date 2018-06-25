package de.fmk.kicknrush;

import de.fmk.kicknrush.db.DatabaseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;


@SpringBootApplication
public class ServerStart implements CommandLineRunner {
	@Autowired
	private JdbcTemplate jdbcTemplate;


	public static void main(String[] args) {
		SpringApplication.run(ServerStart.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		final DatabaseHandler dbHandler;

		dbHandler = new DatabaseHandler(jdbcTemplate);
		dbHandler.createInitialTables();
	}
}

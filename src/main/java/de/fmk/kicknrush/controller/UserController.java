package de.fmk.kicknrush.controller;


import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {
	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RequestMapping(path="/login")
	public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
		final DatabaseHandler dbHandler;
		final User            user;

		dbHandler = new DatabaseHandler(jdbcTemplate);
		user      = dbHandler.findUser(username, password);

		if (user == null)
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

		return new ResponseEntity<>(user, HttpStatus.OK);
	}
}

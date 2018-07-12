package de.fmk.kicknrush.controller;


import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.Session;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path="/api/user")
public class UserController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RequestMapping(path="/login")
	public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
		final DatabaseHandler dbHandler;
		final User            user;

		boolean correctPassword;

		dbHandler = new DatabaseHandler(jdbcTemplate);
		user      = dbHandler.findUser(username);

		if (user == null)
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		correctPassword = password.equals(user.getPassword());

		if (!correctPassword)
			correctPassword = PasswordUtils.verifyUserPassword(password, user.getPassword(), user.getSalt());

		if (correctPassword) {
			if (dbHandler.addSession(user))
				return new ResponseEntity<>(user, HttpStatus.OK);
			else
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}


	@RequestMapping(path="/logout")
	public ResponseEntity<String> logout(@RequestParam String userID) {
		final DatabaseHandler dbHandler;

		dbHandler = new DatabaseHandler(jdbcTemplate);

		if (dbHandler.closeSession(userID))
			return new ResponseEntity<>("Successfully logged out.", HttpStatus.OK);

		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@RequestMapping(path="/getUsernames")
	public ResponseEntity<List<String>> getUsernames() {
		final DatabaseHandler dbHandler;
		final List<String>    usernames;

		dbHandler = new DatabaseHandler(jdbcTemplate);
		usernames = dbHandler.getUsernames();

		return new ResponseEntity<>(usernames, HttpStatus.OK);
	}


	@PostMapping("/updateUser")
	public ResponseEntity<Boolean> updateUser(@RequestBody MultiValueMap<String, String> body) {
		final DatabaseHandler dbHandler;
		final Session         session;
		final String          password;
		final String          salt;
		final String          sessionID;
		final String          username;

		sessionID = body.getFirst("session");

		if (sessionID == null || sessionID.isEmpty())
			return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);

		dbHandler = new DatabaseHandler(jdbcTemplate);
		session   = dbHandler.getSessionForID(sessionID);

		if (session == null)
			return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);

		username = body.getFirst("username");
		password = body.getFirst("password");
		salt     = body.getFirst("salt");

		if (!dbHandler.updateSession(session.getSessionID()))
			LOGGER.warn("Could not update the session for the user with id '{}'.", session.getUserID());

		if (dbHandler.updateUser(session.getUserID(), username, password, salt))
			return new ResponseEntity<>(true, HttpStatus.OK);

		return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
	}


	@RequestMapping(path="/test")
	public void test(@RequestParam String id) {
		new DatabaseHandler(jdbcTemplate).getSessionForID(id);
	}
}

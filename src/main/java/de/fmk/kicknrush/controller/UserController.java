package de.fmk.kicknrush.controller;


import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path="/api/user")
public class UserController {
	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RequestMapping(path="/login")
	public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
		final DatabaseHandler dbHandler;
		final String          secretPassword;
		final User            user;

		dbHandler = new DatabaseHandler(jdbcTemplate);
		user      = dbHandler.findUser(username);

		if (user == null)
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		secretPassword = PasswordUtils.generateSecurePassword(password, user.getSalt());

		if (PasswordUtils.verifyUserPassword(password, secretPassword, user.getSalt())) {
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

		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}

package de.fmk.kicknrush.controller;


import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.dto.UserDTO;
import de.fmk.kicknrush.models.User;
import de.fmk.kicknrush.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(path="/api/user")
public class UserController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	private static final String SESSION = "session";
	private static final String USER_ID = "userID";
	private static final String INVALID_PARAMETERS        = "'{}' or '{}' is not a valid id.";
	private static final String IS_NOT_A_VALID_SESSION_ID = "'{}' is not a valid session id.";

	@Autowired
	private DatabaseHandler dbHandler;


	@PostMapping("/admin/administrateUser")
	public ResponseEntity<Boolean> administrateUser(@RequestBody MultiValueMap<String, String> body) {
		final User user;
		final UUID sessionID;

		try {
			if (body.getFirst(SESSION) == null)
				sessionID = null;
			else
				sessionID = UUID.fromString(body.getFirst(SESSION));
		}
		catch (IllegalArgumentException iaex) {
			return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);
		}

		if (!dbHandler.isAdminSession(sessionID))
			return new ResponseEntity<>(false, HttpStatus.FORBIDDEN);

		user = new User();
		user.setId(UUID.fromString(body.getFirst("user_id")));
		user.setUsername(body.getFirst("username"));
		user.setAdmin(Boolean.parseBoolean(body.getFirst("admin")));

		if (!dbHandler.updateSession(sessionID))
			LOGGER.warn("Could not update the session.");

//		if (Boolean.parseBoolean(body.getFirst("create.new"))) {
//			if (dbHandler.createUser(user.getUsername(), "1234", null, user.isAdmin()))
//				return new ResponseEntity<>(true, HttpStatus.OK);
//		}
//		else {
//			if (dbHandler.administrateUser(user))
//				return new ResponseEntity<>(true, HttpStatus.OK);
//		}

		return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
	}


	/**
	 * Create a user in the database. Can only be done by an admin.
	 * @param body Request body: Session id, user id and username must be set.
	 * @return {@link org.springframework.http.HttpStatus#OK}, if the user has been successfully created.
	 */
	@PostMapping("/admin/createUser")
	public ResponseEntity<String> createUser(@RequestBody UserDTO body) {
		final ResponseEntity<String> adminResponse;
		final String                 sessionID;
		final String                 userID;
		final String                 username;

		sessionID = body.getSessionID();
		userID    = body.getUserID();
		username  = body.getUsername();

		if (sessionID == null || userID == null || username == null)
			return ResponseEntity.badRequest().build();

		adminResponse = isAdminSession(sessionID);

		if (adminResponse.getStatusCode() != HttpStatus.OK)
			return adminResponse;

		if (!dbHandler.updateSession(UUID.fromString(sessionID)))
			LOGGER.error("Could not update the session with id '{}'.", sessionID);

		if (dbHandler.createUser(User.fromDTO(body)))
			return ResponseEntity.ok().build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}


	/**
	 * Delete a user in the database. Can only be done by an admin.
	 * @param body Request body: Session id and user id must be set.
	 * @return {@link org.springframework.http.HttpStatus#OK}, if the user has been successfully deleted.
	 */
	@PostMapping("/admin/deleteUser")
	public ResponseEntity<String> deleteUser(@RequestBody UserDTO body) {
		final ResponseEntity<String> adminResponse;
		final String                 sessionID;
		final String                 userID;

		sessionID = body.getSessionID();
		userID    = body.getUserID();

		if (sessionID == null || userID == null)
			return ResponseEntity.badRequest().build();

		adminResponse = isAdminSession(sessionID);

		if (adminResponse.getStatusCode() != HttpStatus.OK)
			return adminResponse;

		if (dbHandler.deleteUser(UUID.fromString(userID)))
			return ResponseEntity.ok().build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}


	/**
	 * Search the user with the given username in the database, check the password and create a session, if username and
	 * password are matching.
	 * @param body Request body: Username and password must be set.
	 * @return a user transfer object, that has set the user id, username, salt and the session id.
	 */
	@PostMapping("/login")
	public ResponseEntity<UserDTO> login(@RequestBody UserDTO body) {
		final String password;
		final User   user;

		boolean correctPassword;

		if (body.getUsername() == null || body.getPassword() == null)
			return ResponseEntity.badRequest().build();

		user = dbHandler.findUser(body.getUsername());

		if (user == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

		password        = body.getPassword();
		correctPassword = password.equals(user.getPassword());

		if (!correctPassword && user.getSalt() != null)
			correctPassword = PasswordUtils.verifyUserPassword(password, user.getPassword(), user.getSalt());

		if (correctPassword) {
			final UUID sessionID = dbHandler.createSession(user.getId());

			if (sessionID == null)
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

			user.setSessionID(sessionID);

			return ResponseEntity.ok(user.toDTO());
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}


	/**
	 * Close the session of the user.
	 * @param body Request body: Session id and user id must be set.
	 * @return {@link org.springframework.http.HttpStatus#OK}, if the user has been successfully logged out.
	 */
	@PostMapping(path="/logout")
	public ResponseEntity<String> logout(@RequestBody UserDTO body) {
		final String sessionID;
		final String userID;

		sessionID = body.getSessionID();
		userID    = body.getUserID();

		if (sessionID == null || userID == null)
			return ResponseEntity.badRequest().build();

		try {
			if (dbHandler.closeSession(UUID.fromString(sessionID), userID))
				return ResponseEntity.ok().build();
		}
		catch (IllegalArgumentException iaex) {
			LOGGER.error(IS_NOT_A_VALID_SESSION_ID, sessionID);
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.badRequest().build();
	}


	@PostMapping("/updateUser")
	public ResponseEntity<Boolean> updateUser(@RequestBody UserDTO body) {
		final String                  sessionID;
		final String                  userID;
		final ResponseEntity<Boolean> adminResponse;
		final ResponseEntity<Boolean> sessionResponse;

		sessionID = body.getSessionID();
		userID    = body.getUserID();

		if (sessionID == null || userID == null)
			return ResponseEntity.badRequest().build();

		adminResponse = isAdminSession(sessionID);

		if (adminResponse.getStatusCode() != HttpStatus.OK) {
			sessionResponse = isValidUserSession(sessionID, userID);

			if (sessionResponse.getStatusCode() != HttpStatus.OK)
				return sessionResponse;
		}

		if (!dbHandler.updateSession(UUID.fromString(sessionID)))
			LOGGER.warn("Could not update the session for the user with id '{}'.", userID);

		if (dbHandler.updateUser(User.fromDTO(body)))
			return ResponseEntity.ok().build();

		return ResponseEntity.notFound().build();
	}


	private <T> ResponseEntity<T> isAdminSession(final String sessionID) {
		try {
			if (dbHandler.isAdminSession(UUID.fromString(sessionID)))
				return ResponseEntity.ok().build();
		}
		catch (IllegalArgumentException iaex) {
			LOGGER.error(IS_NOT_A_VALID_SESSION_ID, sessionID);
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}


	private <T> ResponseEntity<T> isValidUserSession(final String sessionID, final String userID) {
		try {
			if (dbHandler.checkSession(UUID.fromString(sessionID), UUID.fromString(userID)))
				return ResponseEntity.ok().build();
		}
		catch (IllegalArgumentException iaex) {
			LOGGER.error(INVALID_PARAMETERS, sessionID, userID);
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}


	/**
	 * Collect all usernames from the database.
	 * @param sessionID ID of an user session.
	 * @param userID ID of the user belonging to the session.
	 * @return an array with all usernames.
	 */
	@RequestMapping(path="/getUsernames")
	public ResponseEntity<String[]> getUsernames(@RequestParam String sessionID, @RequestParam String userID) {
		final List<String>             usernames;
		final ResponseEntity<String[]> sessionResponse;

		sessionResponse = isValidUserSession(sessionID, userID);

		if (sessionResponse.getStatusCode() != HttpStatus.OK)
			return sessionResponse;

		usernames = dbHandler.getUsernames();

		return ResponseEntity.ok(usernames.toArray(new String[0]));
	}


	/**
	 * Collect all users from the database.
	 * @param sessionID ID of an user session.
	 * @param userID ID of the user belonging to the session.
	 * @return an array with all users.
	 */
	@RequestMapping(path="/getUsers")
	public ResponseEntity<UserDTO[]> getUsers(@RequestParam String sessionID, @RequestParam String userID) {
		final List<UserDTO>             users;
		final ResponseEntity<UserDTO[]> sessionResponse;

		sessionResponse = isValidUserSession(sessionID, userID);

		if (sessionResponse.getStatusCode() != HttpStatus.OK)
			return sessionResponse;

		users = new ArrayList<>();

		dbHandler.getUsers().forEach(user -> users.add(user.toDTO()));

		return ResponseEntity.ok(users.toArray(new UserDTO[0]));
	}
}

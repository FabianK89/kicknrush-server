package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;


/**
 * @author FabianK
 */
public final class SessionHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionHelper.class);

	static final String INVALID_PARAMETERS        = "'{}' or '{}' is not a valid id.";
	static final String IS_NOT_A_VALID_SESSION_ID = "'{}' is not a valid session id.";


	static <T> ResponseEntity<T> isAdminSession(final DatabaseHandler dbHandler, final String sessionID) {
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


	static <T> ResponseEntity<T> isValidUserSession(final DatabaseHandler dbHandler,
	                                                final String          sessionID,
	                                                final String          userID) {
		try
		{
			if (dbHandler.checkSession(UUID.fromString(sessionID), UUID.fromString(userID)))
				return ResponseEntity.ok().build();
		}
		catch (IllegalArgumentException iaex)
		{
			LOGGER.error(INVALID_PARAMETERS, sessionID, userID);
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
}

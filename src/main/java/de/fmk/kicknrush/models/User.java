package de.fmk.kicknrush.models;


import de.fmk.kicknrush.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class User {
	private boolean isAdmin;
	private String  password;
	private String  salt;
	private String  username;
	private UUID    id;
	private UUID    sessionID;


	public User(boolean isAdmin, String password, String username, UUID id) {
		this.isAdmin  = isAdmin;
		this.password = password;
		this.username = username;
		this.id       = id;
	}


	/**
	 * Convert the data transfer object to an user object.
	 * @param dto The data transfer object.
	 * @return a user object or <code>null</code>.
	 */
	public static User fromDTO(final UserDTO dto) {
		final User user = new User();

		if (dto == null || dto.getUserID() == null)
			return null;

		try {
			user.setId(UUID.fromString(dto.getUserID()));
			user.setUsername(dto.getUsername());
			user.setSalt(dto.getSalt());
			user.setPassword(dto.getPassword());
			user.setAdmin(dto.isAdmin());
		}
		catch (IllegalArgumentException iaex) {
			return null;
		}

		return user;
	}


	/**
	 * Convert this object to an UserDTO.
	 * @return the data transfer object.
	 */
	public UserDTO toDTO() {
		final UserDTO dto = new UserDTO();

		dto.setUserID(id.toString());
		dto.setSessionID(sessionID == null ? null : sessionID.toString());
		dto.setUsername(username);
		dto.setAdmin(isAdmin);
		dto.setSalt(salt);

		return dto;
	}
}

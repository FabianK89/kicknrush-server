package de.fmk.kicknrush.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * @author FabianK
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserDTO {
	private boolean admin;
	private String  password;
	private String  salt;
	private String  sessionID;
	private String  userID;
	private String  username;
}

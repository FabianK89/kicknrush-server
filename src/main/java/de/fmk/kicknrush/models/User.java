package de.fmk.kicknrush.models;


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
}

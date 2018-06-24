package de.fmk.kicknrush.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AdminAccount {
	private String username;
	private String password;
}

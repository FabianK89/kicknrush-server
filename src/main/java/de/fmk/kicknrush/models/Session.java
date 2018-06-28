package de.fmk.kicknrush.models;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Session {
	private LocalDateTime lastActionTime;
	private LocalDateTime loggedInTime;
	private UUID          sessionID;
	private UUID          userID;
}

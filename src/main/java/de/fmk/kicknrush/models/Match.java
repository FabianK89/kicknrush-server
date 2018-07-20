package de.fmk.kicknrush.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;


/**
 * @author FabianK
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Match {
	private boolean matchIsFinished;
	private long    matchID;
	private String  matchDateTimeUTC;
	private Team    team1;
	private Team    team2;
	private UUID    id;
}

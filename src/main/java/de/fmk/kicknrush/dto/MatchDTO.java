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
@ToString
@EqualsAndHashCode
public class MatchDTO {
	private boolean matchIsFinished;
	private int     groupID;
	private int     matchID;
	private int     teamGuestID;
	private int     teamHomeID;
	private String  matchDateTimeUTC;
}

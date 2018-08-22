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
public class TeamDTO {
	private int    teamId;
	private String teamIcon;
	private String teamIconType;
	private String teamIconSmall;
	private String teamIconSmallType;
	private String teamName;
}

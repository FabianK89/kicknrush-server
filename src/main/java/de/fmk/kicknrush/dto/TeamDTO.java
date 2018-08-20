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
	private String teamIconUrl;
	private String teamIconUrlSmall;
	private String teamName;
}

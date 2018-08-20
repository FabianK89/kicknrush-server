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
public class GroupDTO {
	private int    groupID;
	private int    groupOrderID;
	private int    year;
	private String groupName;
}

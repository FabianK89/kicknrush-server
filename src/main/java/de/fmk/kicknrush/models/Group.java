package de.fmk.kicknrush.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.fmk.kicknrush.dto.GroupDTO;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Group {
	private int    groupID;
	private int    groupOrderID;
	private int    year;
	private String groupName;


	public Group(int groupID) {
		this.groupID = groupID;
	}


	public GroupDTO toDTO() {
		final GroupDTO dto = new GroupDTO();

		dto.setGroupID(groupID);
		dto.setGroupOrderID(groupOrderID);
		dto.setYear(year);
		dto.setGroupName(groupName);

		return dto;
	}
}

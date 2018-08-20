package de.fmk.kicknrush.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.fmk.kicknrush.dto.TeamDTO;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * @author FabianK
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Team {
	private int    teamId;
	private String teamIconUrl;
	private String teamIconUrlSmall;
	private String teamName;


	public Team(int teamId) {
		this.teamId = teamId;
	}


	public TeamDTO toDTO() {
		final TeamDTO dto = new TeamDTO();

		dto.setTeamId(teamId);
		dto.setTeamIconUrl(teamIconUrl);
		dto.setTeamIconUrlSmall(teamIconUrlSmall);
		dto.setTeamName(teamName);

		return dto;
	}
}

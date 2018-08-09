package de.fmk.kicknrush.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.fmk.kicknrush.dto.MatchDTO;
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
public class Match {
	private boolean matchIsFinished;
	private int     matchID;
	private Group   group;
	private String  matchDateTimeUTC;
	private Team    team1;
	private Team    team2;


	public MatchDTO toDTO() {
		final MatchDTO dto = new MatchDTO();

		dto.setMatchID(matchID);
		dto.setGroupID(group.getGroupID());
		dto.setMatchDateTimeUTC(matchDateTimeUTC);
		dto.setTeamGuestID(team2.getTeamId());
		dto.setTeamHomeID(team1.getTeamId());
		dto.setMatchIsFinished(matchIsFinished);

		return dto;
	}
}

package de.fmk.kicknrush.models;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Group {
	private int    groupID;
	private int    groupOrderID;
	private int    year;
	private String groupName;
}

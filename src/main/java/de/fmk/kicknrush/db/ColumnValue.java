package de.fmk.kicknrush.db;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


/**
 * @author FabianK
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ColumnValue {
	private final String name;
	private final Object value;
}

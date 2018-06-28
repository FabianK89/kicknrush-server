package de.fmk.kicknrush.utils;

import org.h2.api.TimestampWithTimeZone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;


public class TimeUtils {
	public static TimestampWithTimeZone createTimestamp(final LocalDateTime time) {
		final short         zoneOffset;
		final ZonedDateTime zonedDateTime;
		final ZoneId        zoneId;

		if (time == null)
			throw new IllegalArgumentException("The time parameter must not be null.");

		zoneId        = TimeZone.getTimeZone("UTC").toZoneId();
		zonedDateTime = time.atZone(zoneId);
		zoneOffset    = 0;

		return new TimestampWithTimeZone(zonedDateTime.toInstant().toEpochMilli(), 0, zoneOffset);
	}


	public static LocalDateTime convertTimestamp(final TimestampWithTimeZone timestamp) {
		final Instant instant;

		if (timestamp == null)
			throw new IllegalArgumentException("The timestamp must not be null.");

		instant = Instant.ofEpochMilli(timestamp.getYMD());

		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}


	private TimeUtils() {}
}

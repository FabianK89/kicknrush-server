package de.fmk.kicknrush.utils;

import org.h2.api.TimestampWithTimeZone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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


	public static TimestampWithTimeZone createTimestamp(final String timeString) {
		final DateTimeFormatter formatter;
		final ZonedDateTime     zonedDateTime;

		if (timeString == null || timeString.isEmpty())
			throw new IllegalArgumentException("The time string parameter must not be null or empty.");

		formatter     = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
		zonedDateTime = ZonedDateTime.parse(timeString, formatter);

		return createTimestamp(zonedDateTime.toLocalDateTime());
	}


	public static LocalDateTime convertTimestamp(final TimestampWithTimeZone timestamp) {
		final Instant instant;

		if (timestamp == null)
			throw new IllegalArgumentException("The timestamp must not be null.");

		instant = Instant.ofEpochMilli(timestamp.getYMD());

		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}


	public static String convertLocalDateTimeUTC(final LocalDateTime time) {
		final DateTimeFormatter formatter;

		if (time == null)
			throw new IllegalArgumentException("The time parameter must not be null.");

		formatter = DateTimeFormatter.ISO_INSTANT.withZone(TimeZone.getTimeZone("UTC").toZoneId());

		return time.atZone(ZoneId.systemDefault()).format(formatter);
	}


	private TimeUtils() {}
}

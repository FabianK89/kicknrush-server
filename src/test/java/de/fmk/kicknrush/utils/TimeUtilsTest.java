package de.fmk.kicknrush.utils;

import org.h2.api.TimestampWithTimeZone;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class TimeUtilsTest
{

	@Test
	public void createTimestamp() {
		final Date                  expected;
		final LocalDateTime         time;
		final TimestampWithTimeZone timestamp;

		try {
			TimeUtils.createTimestamp(null);
			fail("An IllegalArgumentException must occur.");
		}
		catch (Exception ex) {
			assertTrue(ex instanceof IllegalArgumentException);
		}

		time      = LocalDateTime.of(2018, 6, 28, 12, 15);
		timestamp = TimeUtils.createTimestamp(time);
		expected  = Date.from(time.atZone(ZoneId.of("UTC")).toInstant());

		assertNotNull(timestamp);
		assertEquals(expected.getTime(), timestamp.getYMD());
	}


	@Test
	public void convertTimestamp() {
		final Calendar              calendar;
		final LocalDateTime         result;
		final short                 offset;
		final TimestampWithTimeZone timestamp;

		try {
			TimeUtils.convertTimestamp(null);
			fail("An IllegalArgumentException must occur.");
		}
		catch (Exception ex) {
			assertTrue(ex instanceof IllegalArgumentException);
		}

		offset    = 0;
		calendar  = new GregorianCalendar(2018, 5, 28, 12, 15, 30);
		timestamp = new TimestampWithTimeZone(calendar.getTimeInMillis(), 0, offset);
		result    = TimeUtils.convertTimestamp(timestamp);

		assertNotNull(result);
		assertEquals(calendar.get(Calendar.YEAR), result.getYear());
		assertEquals(calendar.get(Calendar.MONTH) + 1, result.getMonthValue());
		assertEquals(calendar.get(Calendar.DAY_OF_MONTH), result.getDayOfMonth());
		assertEquals(calendar.get(Calendar.HOUR_OF_DAY), result.getHour());
		assertEquals(calendar.get(Calendar.MINUTE), result.getMinute());
		assertEquals(calendar.get(Calendar.SECOND), result.getSecond());
	}
}
package ru.artak.utils;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.artak.service.DistanceInterval;
import ru.artak.service.FindIntervalType;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class DateUtilsTest {
	
	private LocalDate now = LocalDate.of(2020, 10, 17);
	
	@Test
	public void shouldReturnCurrentWeekDistance() {
		// given
		FindIntervalType interval = FindIntervalType.CURRENTWEEKDISTANCE;
		
		// when
		DistanceInterval actualResult = DateUtils.getWeekRange(now, interval);
		
		// then
		DistanceInterval expectedResult = new DistanceInterval(
			LocalDate.of(2020, 10, 11).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
			LocalDate.of(2020, 10, 19).atStartOfDay().minusNanos(1).toEpochSecond(ZoneOffset.UTC)
		);
		
		Assertions.assertEquals(expectedResult, actualResult);
	}
	
	
}
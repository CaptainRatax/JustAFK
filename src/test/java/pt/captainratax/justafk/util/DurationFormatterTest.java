package pt.captainratax.justafk.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DurationFormatterTest {

    @Test
    void usesMinutesBelowOneHour() {
        assertEquals("0m", DurationFormatter.format(0L));
        assertEquals("0m", DurationFormatter.format(59_999L));
        assertEquals("1m", DurationFormatter.format(60_000L));
        assertEquals("59m", DurationFormatter.format(3_599_999L));
    }

    @Test
    void usesWholeHoursFromOneHourOnwards() {
        assertEquals("1h", DurationFormatter.format(3_600_000L));
        assertEquals("1h", DurationFormatter.format(7_199_999L));
        assertEquals("2h", DurationFormatter.format(7_200_000L));
    }
}

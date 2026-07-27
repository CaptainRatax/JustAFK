package pt.captainratax.justafk.util;

/**
 * Produces the compact minute and hour labels used in the player list.
 */
public final class DurationFormatter {

    private DurationFormatter() {
    }

    public static String format(long durationMillis) {
        long totalMinutes = Math.max(0L, durationMillis) / 60_000L;
        if (totalMinutes < 60L) {
            return totalMinutes + "m";
        }
        return (totalMinutes / 60L) + "h";
    }
}

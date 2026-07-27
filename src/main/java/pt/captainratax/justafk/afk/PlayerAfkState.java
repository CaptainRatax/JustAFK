package pt.captainratax.justafk.afk;

/**
 * Keeps AFK timing rules separate from Bukkit-side effects.
 */
public final class PlayerAfkState {

    private long lastActivityMillis;
    private long afkSinceMillis = -1L;

    public PlayerAfkState(long nowMillis) {
        lastActivityMillis = nowMillis;
    }

    public AfkTransition recordActivity(long nowMillis) {
        boolean wasAfk = isAfk();
        lastActivityMillis = nowMillis;
        afkSinceMillis = -1L;
        return wasAfk ? AfkTransition.BECAME_ACTIVE : AfkTransition.NONE;
    }

    public AfkTransition setAfk(boolean afk, long nowMillis) {
        if (afk) {
            if (isAfk()) {
                return AfkTransition.NONE;
            }
            afkSinceMillis = nowMillis;
            return AfkTransition.BECAME_AFK;
        }
        // Leaving AFK also starts a fresh inactivity window.
        return recordActivity(nowMillis);
    }

    public AfkTransition tryAutomaticAfk(long nowMillis, long timeoutSeconds) {
        if (isAfk()) {
            return AfkTransition.NONE;
        }

        long elapsedMillis = Math.max(0L, nowMillis - lastActivityMillis);
        if (elapsedMillis / 1_000L < timeoutSeconds) {
            return AfkTransition.NONE;
        }

        afkSinceMillis = nowMillis;
        return AfkTransition.BECAME_AFK;
    }

    public boolean isAfk() {
        return afkSinceMillis >= 0L;
    }

    public long afkDurationMillis(long nowMillis) {
        if (!isAfk()) {
            return 0L;
        }
        return Math.max(0L, nowMillis - afkSinceMillis);
    }
}

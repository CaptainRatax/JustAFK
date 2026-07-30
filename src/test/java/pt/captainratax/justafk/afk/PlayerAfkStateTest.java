package pt.captainratax.justafk.afk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerAfkStateTest {

    @Test
    void becomesAfkAtTheConfiguredTimeout() {
        PlayerAfkState state = new PlayerAfkState(0L);

        assertEquals(AfkTransition.NONE, state.tryAutomaticAfk(299_999L, 300L));
        assertFalse(state.isAfk());

        assertEquals(
            AfkTransition.BECAME_AFK,
            state.tryAutomaticAfk(300_000L, 300L)
        );
        assertTrue(state.isAfk());
    }

    @Test
    void activityInputMakesAnAfkPlayerActive() {
        PlayerAfkState state = new PlayerAfkState(0L);
        state.setAfk(true, 10_000L);

        assertEquals(AfkTransition.BECAME_ACTIVE, state.recordActivity(20_000L));
        assertFalse(state.isAfk());
    }

    @Test
    void repeatedStateChangesDoNotCreateDuplicateTransitions() {
        PlayerAfkState state = new PlayerAfkState(0L);

        assertEquals(AfkTransition.BECAME_AFK, state.setAfk(true, 1_000L));
        assertEquals(AfkTransition.NONE, state.setAfk(true, 2_000L));
        assertEquals(2_000L, state.afkDurationMillis(3_000L));
    }
}

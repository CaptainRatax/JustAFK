package pt.captainratax.justafk.afk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import pt.captainratax.justafk.api.JustAfkApi;
import pt.captainratax.justafk.api.event.PlayerAfkStateChangeEvent;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.platform.ScheduledHandle;

class AfkManagerApiEventTest {

    private static final long TIMEOUT_SECONDS = 300L;
    private static final Input NO_INPUT = new Input() {
        @Override
        public boolean isForward() {
            return false;
        }

        @Override
        public boolean isBackward() {
            return false;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public boolean isJump() {
            return false;
        }

        @Override
        public boolean isSneak() {
            return false;
        }

        @Override
        public boolean isSprint() {
            return false;
        }
    };
    private static final PlatformScheduler IMMEDIATE_SCHEDULER =
        new PlatformScheduler() {
            @Override
            public ScheduledHandle repeatGlobal(
                Runnable task,
                long initialDelayTicks,
                long periodTicks
            ) {
                return () -> {
                };
            }

            @Override
            public void runForPlayer(Player player, Runnable task) {
                task.run();
            }

            @Override
            public void cancelAll() {
            }

            @Override
            public String platformName() {
                return "test";
            }
        };

    @Test
    void apiReportsTrackedPlayerAndUuidState() {
        TestContext context = new TestContext();
        JustAfkApi api = context.manager;

        assertFalse(api.isAfk(context.player));
        assertFalse(api.isAfk(context.playerId));

        context.manager.setAfk(context.player, true);

        assertTrue(api.isAfk(context.player));
        assertTrue(api.isAfk(context.playerId));

        context.manager.unregister(context.player);

        assertFalse(api.isAfk(context.player));
        assertFalse(api.isAfk(context.playerId));
    }

    @Test
    void unknownUuidQueryDoesNotCreateTrackingState() {
        TestContext context = new TestContext();
        JustAfkApi api = context.manager;
        UUID unknownPlayerId = UUID.randomUUID();

        assertFalse(api.isAfk(unknownPlayerId));

        context.nowMillis.set(TIMEOUT_SECONDS * 1_000L);
        context.manager.checkPlayer(player(unknownPlayerId));

        assertFalse(api.isAfk(unknownPlayerId));
    }

    @Test
    void genericActivityRestartsTheInactivityWindow() {
        TestContext context = new TestContext();
        context.manager.register(context.player);

        context.nowMillis.set(299_999L);
        context.manager.recordActivity(context.player);

        context.nowMillis.set(300_000L);
        context.manager.checkPlayer(context.player);
        assertFalse(context.manager.isAfk(context.player));

        context.nowMillis.set(599_999L);
        context.manager.checkPlayer(context.player);
        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void stateChangeEventsContainTheNewStateAndCause() {
        TestContext context = new TestContext();

        context.manager.setAfk(context.player, true);
        context.manager.recordActivity(context.player);

        assertEquals(2, context.events.size());

        PlayerAfkStateChangeEvent becameAfk = context.events.get(0);
        assertSame(context.player, becameAfk.getPlayer());
        assertSame(
            PlayerAfkStateChangeEvent.getHandlerList(),
            becameAfk.getHandlers()
        );
        assertTrue(becameAfk.isAfk());
        assertFalse(becameAfk.isAutomatic());

        PlayerAfkStateChangeEvent becameActive = context.events.get(1);
        assertSame(context.player, becameActive.getPlayer());
        assertFalse(becameActive.isAfk());
        assertFalse(becameActive.isAutomatic());
    }

    @Test
    void automaticStateChangeEventIsMarkedAutomatic() {
        TestContext context = new TestContext();
        context.manager.register(context.player);
        context.nowMillis.set(TIMEOUT_SECONDS * 1_000L);

        context.manager.checkPlayer(context.player);
        context.nowMillis.incrementAndGet();
        context.manager.checkPlayer(context.player);

        assertEquals(1, context.events.size());
        assertTrue(context.events.get(0).isAfk());
        assertTrue(context.events.get(0).isAutomatic());
    }

    @Test
    void repeatedStateOperationsDoNotPublishDuplicateEvents() {
        TestContext context = new TestContext();

        context.manager.setAfk(context.player, true);
        context.manager.setAfk(context.player, true);
        context.manager.recordActivity(context.player);
        context.manager.recordActivity(context.player);

        assertEquals(2, context.events.size());
        assertTrue(context.events.get(0).isAfk());
        assertFalse(context.events.get(1).isAfk());
    }

    @Test
    void clearingTrackedAfkStatePublishesOneActiveEvent() {
        TestContext context = new TestContext();
        context.manager.setAfk(context.player, true);

        context.manager.clearTrackedPlayers(List.of(context.player), true);
        context.manager.clearTrackedPlayers(List.of(context.player), true);

        assertFalse(context.manager.isAfk(context.player));
        assertEquals(2, context.events.size());
        assertTrue(context.events.get(0).isAfk());
        assertFalse(context.events.get(1).isAfk());
    }

    private static PluginSettings settings() {
        return new PluginSettings(
            true,
            true,
            TIMEOUT_SECONDS,
            AnnouncementAudience.NONE,
            false,
            "&7[AFK {duration}]&r ",
            "",
            "{player} is now AFK.",
            "{player} is no longer AFK."
        );
    }

    private static Player player(UUID playerId) {
        InvocationHandler handler = new TestPlayerHandler(playerId);
        return (Player) Proxy.newProxyInstance(
            Player.class.getClassLoader(),
            new Class<?>[] {Player.class},
            handler
        );
    }

    private static final class TestContext {

        final AtomicLong nowMillis = new AtomicLong();
        final UUID playerId = UUID.randomUUID();
        final Player player = player(playerId);
        final List<PlayerAfkStateChangeEvent> events = new ArrayList<>();
        final AfkManager manager = new AfkManager(
            AfkManagerApiEventTest::settings,
            IMMEDIATE_SCHEDULER,
            nowMillis::get,
            events::add
        );
    }

    private static final class TestPlayerHandler implements InvocationHandler {

        private final UUID playerId;

        TestPlayerHandler(UUID playerId) {
            this.playerId = playerId;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            String methodName = method.getName();
            if ("getUniqueId".equals(methodName)) {
                return playerId;
            }
            if ("getName".equals(methodName)) {
                return "TestPlayer";
            }
            if ("isOnline".equals(methodName)) {
                return true;
            }
            if ("getCurrentInput".equals(methodName)) {
                return NO_INPUT;
            }
            if ("toString".equals(methodName)) {
                return "TestPlayer";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == arguments[0];
            }

            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0.0F;
            }
            if (returnType == double.class) {
                return 0.0D;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return null;
        }
    }
}

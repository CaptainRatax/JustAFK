package pt.captainratax.justafk.afk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.platform.ScheduledHandle;

class AfkManagerInputTest {

    private static final long TIMEOUT_SECONDS = 300L;
    private static final Input NO_INPUT = input(null);
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

    @ParameterizedTest
    @EnumSource(MovementFlag.class)
    void everyMovementInputMakesAnAfkPlayerActive(MovementFlag flag) {
        TestContext context = new TestContext(settings(true, true));
        context.manager.setAfk(context.player, true);

        context.manager.recordInput(context.player, input(flag));

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void inputUpdateWithoutActiveFlagsKeepsPlayerAfk() {
        TestContext context = new TestContext(settings(true, true));
        context.manager.setAfk(context.player, true);

        context.manager.recordInput(context.player, NO_INPUT);

        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void tapBetweenMonitorPassesRestartsTheInactivityWindow() {
        TestContext context = new TestContext(settings(true, true));
        context.manager.register(context.player);

        context.nowMillis.set(299_999L);
        context.manager.recordInput(
            context.player,
            input(MovementFlag.FORWARD)
        );
        context.currentInput.set(NO_INPUT);

        context.nowMillis.set(300_000L);
        context.manager.checkPlayer(context.player);
        assertFalse(context.manager.isAfk(context.player));

        context.nowMillis.set(599_999L);
        context.manager.checkPlayer(context.player);
        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void heldInputIsDetectedByPolling() {
        TestContext context = new TestContext(settings(true, true));
        context.manager.register(context.player);
        context.currentInput.set(input(MovementFlag.SNEAK));

        context.nowMillis.set(300_000L);
        context.manager.checkPlayer(context.player);
        context.nowMillis.set(600_000L);
        context.manager.checkPlayer(context.player);
        context.nowMillis.set(900_000L);
        context.manager.checkPlayer(context.player);

        assertFalse(context.manager.isAfk(context.player));

        context.currentInput.set(NO_INPUT);
        context.nowMillis.set(1_199_999L);
        context.manager.checkPlayer(context.player);
        assertFalse(context.manager.isAfk(context.player));

        context.nowMillis.set(1_200_000L);
        context.manager.checkPlayer(context.player);
        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void externalPositionChangesCannotResetTheTimeout() {
        TestContext context = new TestContext(settings(true, true));
        context.manager.register(context.player);
        context.currentInput.set(NO_INPUT);

        context.nowMillis.set(299_999L);
        context.manager.checkPlayer(context.player);
        context.nowMillis.set(300_000L);
        context.manager.checkPlayer(context.player);

        assertTrue(context.manager.isAfk(context.player));
        assertEquals(0, context.playerHandler.locationReads.get());
    }

    @Test
    void disablingAutomaticAfkStillAllowsInputToClearManualAfk() {
        TestContext context = new TestContext(settings(true, false));
        context.manager.setAfk(context.player, true);

        context.nowMillis.set(1_000_000L);
        context.currentInput.set(NO_INPUT);
        context.manager.checkPlayer(context.player);
        assertTrue(context.manager.isAfk(context.player));

        context.manager.recordInput(context.player, input(MovementFlag.JUMP));
        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void disabledPluginIgnoresInputAndPolling() {
        TestContext context = new TestContext(settings(false, true));

        context.manager.recordInput(
            context.player,
            input(MovementFlag.SPRINT)
        );
        context.manager.checkPlayer(context.player);

        assertFalse(context.manager.isAfk(context.player));
        assertEquals(0, context.playerHandler.currentInputReads.get());
    }

    private static PluginSettings settings(
        boolean enabled,
        boolean automaticAfkEnabled
    ) {
        return new PluginSettings(
            enabled,
            automaticAfkEnabled,
            TIMEOUT_SECONDS,
            AnnouncementAudience.NONE,
            false,
            "&7[AFK {duration}]&r ",
            "",
            "{player} is now AFK.",
            "{player} is no longer AFK."
        );
    }

    private static Input input(MovementFlag activeFlag) {
        return new Input() {
            @Override
            public boolean isForward() {
                return activeFlag == MovementFlag.FORWARD;
            }

            @Override
            public boolean isBackward() {
                return activeFlag == MovementFlag.BACKWARD;
            }

            @Override
            public boolean isLeft() {
                return activeFlag == MovementFlag.LEFT;
            }

            @Override
            public boolean isRight() {
                return activeFlag == MovementFlag.RIGHT;
            }

            @Override
            public boolean isJump() {
                return activeFlag == MovementFlag.JUMP;
            }

            @Override
            public boolean isSneak() {
                return activeFlag == MovementFlag.SNEAK;
            }

            @Override
            public boolean isSprint() {
                return activeFlag == MovementFlag.SPRINT;
            }
        };
    }

    private enum MovementFlag {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        JUMP,
        SNEAK,
        SPRINT
    }

    private static final class TestContext {

        final AtomicLong nowMillis = new AtomicLong();
        final AtomicReference<Input> currentInput = new AtomicReference<>(NO_INPUT);
        final TestPlayerHandler playerHandler = new TestPlayerHandler(currentInput);
        final Player player = playerHandler.createProxy();
        final AfkManager manager;

        TestContext(PluginSettings initialSettings) {
            AtomicReference<PluginSettings> settings =
                new AtomicReference<>(initialSettings);
            manager = new AfkManager(
                settings::get,
                IMMEDIATE_SCHEDULER,
                nowMillis::get
            );
        }
    }

    private static final class TestPlayerHandler implements InvocationHandler {

        private final UUID playerId = UUID.randomUUID();
        private final AtomicReference<Input> currentInput;
        final AtomicInteger currentInputReads = new AtomicInteger();
        final AtomicInteger locationReads = new AtomicInteger();

        TestPlayerHandler(AtomicReference<Input> currentInput) {
            this.currentInput = currentInput;
        }

        Player createProxy() {
            return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[] {Player.class},
                this
            );
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
                currentInputReads.incrementAndGet();
                return currentInput.get();
            }
            if ("getLocation".equals(methodName)) {
                locationReads.incrementAndGet();
                return null;
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

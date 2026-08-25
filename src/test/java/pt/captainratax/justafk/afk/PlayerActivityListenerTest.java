package pt.captainratax.justafk.afk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.listener.PlayerActivityListener;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.platform.ScheduledHandle;

class PlayerActivityListenerTest {

    private static final PluginSettings SETTINGS = new PluginSettings(
        true,
        true,
        300L,
        AnnouncementAudience.NONE,
        false,
        "&7[AFK {duration}]&r ",
        "",
        "{player} is now AFK.",
        "{player} is no longer AFK."
    );

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
    void cancelledBlockBreakMakesAnAfkPlayerActive() {
        TestContext context = new TestContext();
        BlockBreakEvent event = new BlockBreakEvent(
            proxy(Block.class),
            context.player
        );
        event.setCancelled(true);

        context.makeAfk();
        context.listener.onBlockBreak(event);

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void cancelledBlockPlaceMakesAnAfkPlayerActive() {
        TestContext context = new TestContext();
        Block block = proxy(Block.class);
        BlockPlaceEvent event = new BlockPlaceEvent(
            block,
            proxy(BlockState.class),
            block,
            new ItemStack(Material.STONE),
            context.player,
            true,
            EquipmentSlot.HAND
        );
        event.setCancelled(true);

        context.makeAfk();
        context.listener.onBlockPlace(event);

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void cancelledFishingMakesAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerFishEvent event = new PlayerFishEvent(
            context.player,
            null,
            proxy(FishHook.class),
            EquipmentSlot.HAND,
            PlayerFishEvent.State.FISHING
        );
        event.setCancelled(true);

        context.makeAfk();
        context.listener.onPlayerFish(event);

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void serverDrivenFishingBiteDoesNotMakeAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerFishEvent event = new PlayerFishEvent(
            context.player,
            null,
            proxy(FishHook.class),
            EquipmentSlot.HAND,
            PlayerFishEvent.State.BITE
        );

        context.makeAfk();
        context.listener.onPlayerFish(event);

        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void cancelledBlockAndItemInteractionsMakeAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerInteractEvent blockInteraction = new PlayerInteractEvent(
            context.player,
            Action.RIGHT_CLICK_BLOCK,
            null,
            proxy(Block.class),
            BlockFace.UP,
            EquipmentSlot.HAND
        );
        blockInteraction.setCancelled(true);

        context.makeAfk();
        context.listener.onPlayerInteract(blockInteraction);
        assertFalse(context.manager.isAfk(context.player));

        PlayerInteractEvent itemInteraction = new PlayerInteractEvent(
            context.player,
            Action.RIGHT_CLICK_AIR,
            new ItemStack(Material.STICK),
            null,
            BlockFace.UP,
            EquipmentSlot.HAND
        );
        itemInteraction.setCancelled(true);

        context.makeAfk();
        context.listener.onPlayerInteract(itemInteraction);
        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void physicalInteractionWithoutInputDoesNotMakeAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerInteractEvent event = new PlayerInteractEvent(
            context.player,
            Action.PHYSICAL,
            null,
            proxy(Block.class),
            BlockFace.UP,
            EquipmentSlot.HAND
        );

        context.makeAfk();
        context.listener.onPlayerInteract(event);

        assertTrue(context.manager.isAfk(context.player));
    }

    @Test
    void cancelledEntityInteractionMakesAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
            context.player,
            proxy(Entity.class),
            EquipmentSlot.HAND
        );
        event.setCancelled(true);

        context.makeAfk();
        context.listener.onPlayerInteractEntity(event);

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void cancelledPreciseEntityInteractionMakesAnAfkPlayerActive() {
        TestContext context = new TestContext();
        PlayerInteractAtEntityEvent event = new PlayerInteractAtEntityEvent(
            context.player,
            proxy(Entity.class),
            new Vector(0.5D, 0.5D, 0.5D),
            EquipmentSlot.HAND
        );
        event.setCancelled(true);

        context.makeAfk();
        context.listener.onPlayerInteractAtEntity(event);

        assertFalse(context.manager.isAfk(context.player));
    }

    @Test
    void activityHandlersMonitorCancelledEvents() throws NoSuchMethodException {
        assertCancelledEventsObserved("onBlockBreak", BlockBreakEvent.class);
        assertCancelledEventsObserved("onBlockPlace", BlockPlaceEvent.class);
        assertCancelledEventsObserved("onPlayerFish", PlayerFishEvent.class);
        assertCancelledEventsObserved("onPlayerInteract", PlayerInteractEvent.class);
        assertCancelledEventsObserved(
            "onPlayerInteractEntity",
            PlayerInteractEntityEvent.class
        );
        assertCancelledEventsObserved(
            "onPlayerInteractAtEntity",
            PlayerInteractAtEntityEvent.class
        );
    }

    private static void assertCancelledEventsObserved(
        String methodName,
        Class<? extends Event> eventType
    ) throws NoSuchMethodException {
        EventHandler handler = PlayerActivityListener.class
            .getMethod(methodName, eventType)
            .getAnnotation(EventHandler.class);

        assertNotNull(handler);
        assertEquals(EventPriority.MONITOR, handler.priority());
        assertFalse(handler.ignoreCancelled());
    }

    private static <T> T proxy(Class<T> type) {
        InvocationHandler handler = (instance, method, arguments) ->
            defaultValue(instance, method, arguments);
        return type.cast(Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[] {type},
            handler
        ));
    }

    private static Object defaultValue(
        Object instance,
        Method method,
        Object[] arguments
    ) {
        String methodName = method.getName();
        if ("toString".equals(methodName)) {
            return instance.getClass().getInterfaces()[0].getSimpleName() + "Proxy";
        }
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(instance);
        }
        if ("equals".equals(methodName)) {
            return instance == arguments[0];
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

    private static final class TestContext {

        final AtomicLong nowMillis = new AtomicLong();
        final UUID playerId = UUID.randomUUID();
        final Player player = createPlayer();
        final AfkManager manager = new AfkManager(
            () -> SETTINGS,
            IMMEDIATE_SCHEDULER,
            nowMillis::get,
            ignored -> {
            }
        );
        final PlayerActivityListener listener = new PlayerActivityListener(manager);

        void makeAfk() {
            assertTrue(manager.setAfk(player, true));
        }

        private Player createPlayer() {
            InvocationHandler handler = (instance, method, arguments) -> {
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
                return defaultValue(instance, method, arguments);
            };
            return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[] {Player.class},
                handler
            );
        }
    }
}

package pt.captainratax.justafk.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reflective Folia adapter that keeps the same JAR loadable on Bukkit servers.
 */
final class FoliaPlatformScheduler implements PlatformScheduler {

    private final JavaPlugin plugin;
    private final Object globalScheduler;
    private final Method runAtFixedRate;
    private final Method cancelGlobalTasks;
    private final Method getEntityScheduler;
    private final Method executeEntityTask;
    private final Method cancelScheduledTask;

    FoliaPlatformScheduler(JavaPlugin plugin) {
        this.plugin = plugin;

        try {
            // Resolve the Folia methods once at startup instead of on every task.
            Class<?> globalSchedulerType = Class.forName(
                "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler"
            );
            Class<?> entitySchedulerType = Class.forName(
                "io.papermc.paper.threadedregions.scheduler.EntityScheduler"
            );
            Class<?> scheduledTaskType = Class.forName(
                "io.papermc.paper.threadedregions.scheduler.ScheduledTask"
            );

            Method getGlobalScheduler = plugin.getServer().getClass().getMethod(
                "getGlobalRegionScheduler"
            );
            globalScheduler = getGlobalScheduler.invoke(plugin.getServer());
            runAtFixedRate = globalSchedulerType.getMethod(
                "runAtFixedRate",
                Plugin.class,
                Consumer.class,
                long.class,
                long.class
            );
            cancelGlobalTasks = globalSchedulerType.getMethod(
                "cancelTasks",
                Plugin.class
            );
            getEntityScheduler = Entity.class.getMethod("getScheduler");
            executeEntityTask = entitySchedulerType.getMethod(
                "execute",
                Plugin.class,
                Runnable.class,
                Runnable.class,
                long.class
            );
            cancelScheduledTask = scheduledTaskType.getMethod("cancel");
        } catch (ReflectiveOperationException exception) {
            throw reflectionFailure("initialise the Folia scheduler", exception);
        }
    }

    @Override
    public ScheduledHandle repeatGlobal(
        Runnable task,
        long initialDelayTicks,
        long periodTicks
    ) {
        try {
            Consumer<Object> callback = ignored -> task.run();
            Object scheduledTask = runAtFixedRate.invoke(
                globalScheduler,
                plugin,
                callback,
                initialDelayTicks,
                periodTicks
            );
            return () -> cancelTask(scheduledTask);
        } catch (ReflectiveOperationException exception) {
            throw reflectionFailure("schedule a Folia global task", exception);
        }
    }

    @Override
    public void runForPlayer(Player player, Runnable task) {
        try {
            Object entityScheduler = getEntityScheduler.invoke(player);
            executeEntityTask.invoke(entityScheduler, plugin, task, null, 1L);
        } catch (ReflectiveOperationException exception) {
            throw reflectionFailure("schedule a Folia player task", exception);
        }
    }

    @Override
    public void cancelAll() {
        try {
            cancelGlobalTasks.invoke(globalScheduler, plugin);
        } catch (ReflectiveOperationException exception) {
            throw reflectionFailure("cancel Folia tasks", exception);
        }
    }

    @Override
    public String platformName() {
        return "Folia scheduler";
    }

    private void cancelTask(Object task) {
        if (task == null) {
            return;
        }
        try {
            cancelScheduledTask.invoke(task);
        } catch (ReflectiveOperationException exception) {
            throw reflectionFailure("cancel a Folia task", exception);
        }
    }

    private IllegalStateException reflectionFailure(
        String action,
        ReflectiveOperationException exception
    ) {
        Throwable cause = exception instanceof InvocationTargetException invocation
            && invocation.getCause() != null
            ? invocation.getCause()
            : exception;
        return new IllegalStateException("Could not " + action + ".", cause);
    }
}

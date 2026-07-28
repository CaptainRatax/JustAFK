package pt.captainratax.justafk.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pt.captainratax.justafk.afk.AfkManager;
import pt.captainratax.justafk.config.JustAfkConfig;
import pt.captainratax.justafk.platform.PlatformScheduler;
import pt.captainratax.justafk.util.ColorText;
import pt.captainratax.justafk.util.CommandMessages;

/**
 * Handles self-service AFK toggles and operator changes for online players.
 */
public final class AfkCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ACTIONS = Arrays.asList("on", "off", "toggle");

    private final AfkManager afkManager;
    private final PlatformScheduler scheduler;
    private final JustAfkConfig config;

    public AfkCommand(
        AfkManager afkManager,
        PlatformScheduler scheduler,
        JustAfkConfig config
    ) {
        this.afkManager = afkManager;
        this.scheduler = scheduler;
        this.config = config;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (args.length == 0) {
            return toggleSelf(sender);
        }

        if (!sender.hasPermission("justafk.others")) {
            CommandMessages.send(
                sender,
                config,
                "&cYou do not have permission to change another player's AFK state."
            );
            return true;
        }

        if (args.length > 2) {
            CommandMessages.send(sender, config, "&cUsage: /afk <player> [on|off|toggle]");
            return true;
        }

        Player target = findOnlinePlayer(args[0]);
        if (target == null) {
            CommandMessages.send(sender, config, "&cThat player is not online.");
            return true;
        }

        String action = args.length == 2
            ? args[1].toLowerCase(Locale.ROOT)
            : "toggle";
        if (!ACTIONS.contains(action)) {
            CommandMessages.send(sender, config, "&cUse on, off, or toggle.");
            return true;
        }

        // The target may belong to a different Folia region.
        scheduler.runForPlayer(target, () -> {
            boolean isAfk;
            if ("on".equals(action)) {
                isAfk = afkManager.setAfk(target, true);
            } else if ("off".equals(action)) {
                isAfk = afkManager.setAfk(target, false);
            } else {
                isAfk = afkManager.toggleAfk(target);
            }

            sendResult(
                sender,
                target.getName() + " is " + (isAfk ? "now AFK." : "no longer AFK.")
            );

            if (!sender.getName().equalsIgnoreCase(target.getName())) {
                target.sendMessage(ColorText.colorize(
                    config.settings().commandPrefix()
                        + "&7Your AFK state was changed by "
                        + sender.getName()
                        + "."
                ));
            }
        });
        return true;
    }

    private boolean toggleSelf(CommandSender sender) {
        if (!(sender instanceof Player)) {
            CommandMessages.send(
                sender,
                config,
                "&cThe console must use /afk <player> [on|off|toggle]."
            );
            return true;
        }

        Player player = (Player) sender;
        boolean isAfk = afkManager.toggleAfk(player);
        CommandMessages.send(
            sender,
            config,
            isAfk ? "&7You are now AFK." : "&7You are no longer AFK."
        );
        return true;
    }

    private Player findOnlinePlayer(String name) {
        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    private void sendResult(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            scheduler.runForPlayer(
                player,
                () -> CommandMessages.send(player, config, "&7" + message)
            );
        } else {
            CommandMessages.send(sender, config, "&7" + message);
        }
    }

    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        if (!sender.hasPermission("justafk.others")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> names = Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getName())
                .collect(Collectors.toList());
            return filter(names, args[0]);
        }
        if (args.length == 2) {
            return filter(ACTIONS, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                matches.add(option);
            }
        }
        return matches;
    }
}

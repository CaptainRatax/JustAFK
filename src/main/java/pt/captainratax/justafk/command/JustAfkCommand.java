package pt.captainratax.justafk.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import pt.captainratax.justafk.afk.AfkManager;
import pt.captainratax.justafk.config.AnnouncementAudience;
import pt.captainratax.justafk.config.JustAfkConfig;
import pt.captainratax.justafk.config.PluginSettings;
import pt.captainratax.justafk.util.CommandMessages;

/**
 * Handles the operator-facing status, reload, and configuration commands.
 */
public final class JustAfkCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ROOT_OPTIONS = Arrays.asList(
        "help",
        "status",
        "reload",
        "set"
    );
    private static final List<String> SETTING_OPTIONS = Arrays.asList(
        "enabled",
        "automatic-afk",
        "timeout",
        "announcements",
        "playerlist"
    );

    private final JustAfkConfig config;
    private final AfkManager afkManager;

    public JustAfkCommand(JustAfkConfig config, AfkManager afkManager) {
        this.config = config;
        this.afkManager = afkManager;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "status":
                return showStatus(sender);
            case "reload":
                return reload(sender);
            case "set":
                return setValue(sender, args);
            default:
                CommandMessages.send(sender, config, "&cUnknown subcommand. Use /justafk help.");
                return true;
        }
    }

    private boolean showStatus(CommandSender sender) {
        PluginSettings settings = config.settings();
        CommandMessages.send(sender, config, "&7Current configuration:");
        sender.sendMessage("  JustAFK: " + enabledLabel(settings.enabled()));
        sender.sendMessage(
            "  Automatic AFK: " + enabledLabel(settings.automaticAfkEnabled())
        );
        sender.sendMessage("  Timeout: " + settings.inactivityTimeoutSeconds() + " seconds");
        sender.sendMessage("  Announcements: " + settings.announcementAudience().configValue());
        sender.sendMessage(
            "  Player list: " + (settings.showInPlayerList() ? "enabled" : "disabled")
        );
        return true;
    }

    private boolean reload(CommandSender sender) {
        try {
            config.reload();
            afkManager.applyConfiguration();
            CommandMessages.send(sender, config, "&aConfiguration reloaded.");
        } catch (Exception exception) {
            CommandMessages.send(
                sender,
                config,
                "&cCould not reload config.yml: " + exception.getMessage()
            );
        }
        return true;
    }

    private boolean setValue(CommandSender sender, String[] args) {
        if (args.length != 3) {
            CommandMessages.send(
                sender,
                config,
                "&cUsage: /justafk set "
                    + "<enabled|automatic-afk|timeout|announcements|playerlist> <value>"
            );
            return true;
        }

        String setting = args[1].toLowerCase(Locale.ROOT);
        String value = args[2];

        try {
            switch (setting) {
                case "enabled":
                    setEnabled(sender, value);
                    break;
                case "automatic-afk":
                    setAutomaticAfk(sender, value);
                    break;
                case "timeout":
                    setTimeout(sender, value);
                    break;
                case "announcements":
                    setAnnouncements(sender, value);
                    break;
                case "playerlist":
                    setPlayerList(sender, value);
                    break;
                default:
                    CommandMessages.send(
                        sender,
                        config,
                        "&cUnknown setting. Use enabled, automatic-afk, timeout, "
                            + "announcements, or playerlist."
                    );
                    break;
            }
        } catch (IOException exception) {
            CommandMessages.send(
                sender,
                config,
                "&cCould not save config.yml: " + exception.getMessage()
            );
        } catch (IllegalArgumentException exception) {
            CommandMessages.send(sender, config, "&c" + exception.getMessage());
        }
        return true;
    }

    private void setEnabled(CommandSender sender, String value) throws IOException {
        boolean enabled = requireBoolean(value, "enabled");
        config.setEnabled(enabled);
        afkManager.applyConfiguration();
        CommandMessages.send(
            sender,
            config,
            "&aJustAFK " + (enabled ? "enabled." : "disabled.")
        );
    }

    private void setAutomaticAfk(CommandSender sender, String value) throws IOException {
        boolean enabled = requireBoolean(value, "automatic-afk");
        config.setAutomaticAfkEnabled(enabled);
        CommandMessages.send(
            sender,
            config,
            "&aAutomatic AFK " + (enabled ? "enabled." : "disabled.")
        );
    }

    private void setTimeout(CommandSender sender, String value) throws IOException {
        long seconds;
        try {
            seconds = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("The timeout must be a whole number of seconds.");
        }

        if (seconds < 1L) {
            throw new IllegalArgumentException("The timeout must be at least 1 second.");
        }

        config.setInactivityTimeoutSeconds(seconds);
        CommandMessages.send(sender, config, "&aTimeout set to " + seconds + " seconds.");
    }

    private void setAnnouncements(CommandSender sender, String value) throws IOException {
        AnnouncementAudience audience = AnnouncementAudience.fromConfig(value);
        config.setAnnouncementAudience(audience);
        CommandMessages.send(
            sender,
            config,
            "&aAnnouncement audience set to " + audience.configValue() + "."
        );
    }

    private void setPlayerList(CommandSender sender, String value) throws IOException {
        boolean enabled = requireBoolean(value, "playerlist");

        config.setPlayerListEnabled(enabled);
        afkManager.refreshPlayerLists();
        CommandMessages.send(
            sender,
            config,
            "&aPlayer-list prefix " + (enabled ? "enabled." : "disabled.")
        );
    }

    private boolean requireBoolean(String value, String setting) {
        Boolean enabled = parseBoolean(value);
        if (enabled == null) {
            throw new IllegalArgumentException("Use on or off for " + setting + ".");
        }
        return enabled;
    }

    private Boolean parseBoolean(String value) {
        switch (value.toLowerCase(Locale.ROOT)) {
            case "on":
            case "true":
            case "yes":
            case "enabled":
                return Boolean.TRUE;
            case "off":
            case "false":
            case "no":
            case "disabled":
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    private void sendHelp(CommandSender sender) {
        CommandMessages.send(sender, config, "&7Commands:");
        sender.sendMessage("  /afk");
        sender.sendMessage("  /afk <player> [on|off|toggle]");
        sender.sendMessage("  /justafk status");
        sender.sendMessage("  /justafk reload");
        sender.sendMessage("  /justafk set enabled <on|off>");
        sender.sendMessage("  /justafk set automatic-afk <on|off>");
        sender.sendMessage("  /justafk set timeout <seconds>");
        sender.sendMessage("  /justafk set announcements <all|ops|none>");
        sender.sendMessage("  /justafk set playerlist <on|off>");
    }

    @Override
    public List<String> onTabComplete(
        CommandSender sender,
        Command command,
        String alias,
        String[] args
    ) {
        if (args.length == 1) {
            return filter(ROOT_OPTIONS, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filter(SETTING_OPTIONS, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "timeout":
                    return filter(Collections.singletonList("300"), args[2]);
                case "announcements":
                    return filter(Arrays.asList("all", "ops", "none"), args[2]);
                case "enabled":
                case "automatic-afk":
                case "playerlist":
                    return filter(Arrays.asList("on", "off"), args[2]);
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private String enabledLabel(boolean enabled) {
        return enabled ? "enabled" : "disabled";
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

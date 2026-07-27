package pt.captainratax.justafk.util;

import org.bukkit.command.CommandSender;
import pt.captainratax.justafk.config.JustAfkConfig;

public final class CommandMessages {

    private CommandMessages() {
    }

    public static void send(
        CommandSender sender,
        JustAfkConfig config,
        String message
    ) {
        sender.sendMessage(
            ColorText.colorize(config.settings().commandPrefix() + message)
        );
    }
}

package pt.captainratax.justafk.util;

import org.bukkit.ChatColor;

public final class ColorText {

    private ColorText() {
    }

    @SuppressWarnings("deprecation")
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

package pt.captainratax.justafk.afk;

import org.bukkit.Input;

/**
 * Defines the client movement inputs that count as player activity.
 */
final class PlayerInputActivity {

    private PlayerInputActivity() {
    }

    static boolean isActive(Input input) {
        return input.isForward()
            || input.isBackward()
            || input.isLeft()
            || input.isRight()
            || input.isJump()
            || input.isSneak()
            || input.isSprint();
    }
}

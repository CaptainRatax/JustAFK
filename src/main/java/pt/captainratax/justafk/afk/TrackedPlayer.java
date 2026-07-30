package pt.captainratax.justafk.afk;

final class TrackedPlayer {

    final PlayerAfkState afkState;
    String originalPlayerListName;
    String lastAppliedPlayerListName;
    String lastDurationLabel;

    TrackedPlayer(long nowMillis) {
        this.afkState = new PlayerAfkState(nowMillis);
    }
}

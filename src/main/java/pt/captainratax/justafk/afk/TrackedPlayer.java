package pt.captainratax.justafk.afk;

final class TrackedPlayer {

    final PlayerAfkState afkState;
    PositionSnapshot lastPosition;
    String originalPlayerListName;
    String lastAppliedPlayerListName;
    String lastDurationLabel;

    TrackedPlayer(long nowMillis, PositionSnapshot lastPosition) {
        this.afkState = new PlayerAfkState(nowMillis);
        this.lastPosition = lastPosition;
    }
}

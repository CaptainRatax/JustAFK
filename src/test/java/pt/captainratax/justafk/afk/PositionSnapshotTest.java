package pt.captainratax.justafk.afk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PositionSnapshotTest {

    @Test
    void comparesOnlyWorldAndCoordinates() {
        UUID worldId = UUID.randomUUID();
        PositionSnapshot first = new PositionSnapshot(worldId, 10.0, 64.0, -5.0);
        PositionSnapshot same = new PositionSnapshot(worldId, 10.0, 64.0, -5.0);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, new PositionSnapshot(worldId, 10.0, 64.0, -4.0));
        assertNotEquals(first, new PositionSnapshot(UUID.randomUUID(), 10.0, 64.0, -5.0));
    }
}

package pt.captainratax.justafk.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AnnouncementAudienceTest {

    @Test
    void parsesAudienceWithoutCaseSensitivity() {
        assertEquals(AnnouncementAudience.ALL, AnnouncementAudience.fromConfig("all"));
        assertEquals(AnnouncementAudience.OPS, AnnouncementAudience.fromConfig("OPS"));
        assertEquals(AnnouncementAudience.NONE, AnnouncementAudience.fromConfig(" None "));
    }

    @Test
    void rejectsUnknownAudience() {
        assertThrows(
            IllegalArgumentException.class,
            () -> AnnouncementAudience.fromConfig("friends")
        );
    }
}

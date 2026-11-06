package config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void portReturnsConfiguredPort() {
        assertEquals(6100, Config.port("A"));
        assertEquals(6122, Config.port("B2"));
    }

    @Test
    void portRejectsUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> Config.port("Z9"));
    }

    @Test
    void indexOfIsZeroBasedWithinGroup() {
        assertEquals(0, Config.indexOf("A1"));
        assertEquals(1, Config.indexOf("B2"));
        assertEquals(2, Config.indexOf("B3"));
    }

    @Test
    void indexOfRejectsHeavyweight() {
        assertThrows(IllegalArgumentException.class, () -> Config.indexOf("A"));
    }

    @Test
    void heavyweightOfReturnsGroupLetter() {
        assertEquals("A", Config.heavyweightOf("A3"));
        assertEquals("B", Config.heavyweightOf("B2"));
    }

    @Test
    void heavyweightOfRejectsHeavyweight() {
        assertThrows(IllegalArgumentException.class, () -> Config.heavyweightOf("A"));
    }

    @Test
    void nextHeavyweightAlternates() {
        assertEquals("B", Config.nextHeavyweight("A"));
        assertEquals("A", Config.nextHeavyweight("B"));
    }

    @Test
    void nextHeavyweightRejectsLightweight() {
        assertThrows(IllegalArgumentException.class, () -> Config.nextHeavyweight("A1"));
    }

    @Test
    void childrenOfListsWholeGroup() {
        assertEquals(List.of("A1", "A2", "A3"), Config.childrenOf("A"));
    }

    @Test
    void groupPeersOfExcludesSelf() {
        assertEquals(List.of("B1", "B3"), Config.groupPeersOf("B2"));
    }

    @Test
    void linkPeersForLightweight() {
        Map<String, Integer> peers = Config.linkPeers("B2");
        assertEquals(Set.of("B1", "B3", "B"), peers.keySet());
        assertEquals(6101, peers.get("B"));
    }

    @Test
    void linkPeersForHeavyweight() {
        Map<String, Integer> peers = Config.linkPeers("A");
        assertEquals(Set.of("A1", "A2", "A3", "B"), peers.keySet());
        assertEquals(6111, peers.get("A1"));
    }
}
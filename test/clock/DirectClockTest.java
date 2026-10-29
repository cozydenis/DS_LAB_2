package clock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectClockTest {

    // Checks all three entries; the message says which entry failed.
    private void assertEntries(DirectClock clock, int a, int b, int c) {
        assertEquals(a, clock.getValue(0), "entry A");
        assertEquals(b, clock.getValue(1), "entry B");
        assertEquals(c, clock.getValue(2), "entry C");
    }

    @Test
    void processAMatchesSlide() {
        DirectClock clock = new DirectClock(3, 0);
        clock.tick();                // e_A1: send to B
        assertEntries(clock, 1, 0, 0);
        clock.tick();                // e_A2: send to C
        assertEntries(clock, 2, 0, 0);
        clock.receiveAction(1, 3);   // e_A3: receive from B, ts 3
        assertEntries(clock, 4, 3, 0);
        clock.tick();                // e_A4: local
        assertEntries(clock, 5, 3, 0);
        clock.receiveAction(2, 1);   // e_A5: receive from C, ts 1
        assertEntries(clock, 6, 3, 1);
        clock.tick();                // e_A6: local
        assertEntries(clock, 7, 3, 1);
    }

    @Test
    void processBMatchesSlide() {
        DirectClock clock = new DirectClock(3, 1);
        clock.receiveAction(0, 1);   // e_B1: receive from A, ts 1
        assertEntries(clock, 1, 2, 0);
        clock.tick();                // e_B2: send to A
        assertEntries(clock, 1, 3, 0);
        clock.tick();                // e_B3: local
        assertEntries(clock, 1, 4, 0);
        clock.tick();                // e_B4: send to C
        assertEntries(clock, 1, 5, 0);
        clock.receiveAction(2, 4);   // e_B5: receive from C, ts 4
        assertEntries(clock, 1, 6, 4);
        assertEquals(6, clock.getValue());   // getValue() = own entry
    }

    @Test
    void processCMatchesSlide() {
        DirectClock clock = new DirectClock(3, 2);
        clock.tick();                // e_C1: send to A
        assertEntries(clock, 0, 0, 1);
        clock.tick();                // e_C2: local
        assertEntries(clock, 0, 0, 2);
        clock.receiveAction(0, 2);   // e_C3: receive from A, ts 2
        assertEntries(clock, 2, 0, 3);
        clock.tick();                // e_C4: send to B
        assertEntries(clock, 2, 0, 4);
        clock.receiveAction(1, 5);   // e_C5: receive from B, ts 5
        assertEntries(clock, 2, 5, 6);
    }

    @Test
    void olderMessageDoesNotLowerSenderEntry() {
        DirectClock clock = new DirectClock(3, 1);
        clock.receiveAction(0, 5);
        assertEntries(clock, 5, 6, 0);
        clock.receiveAction(0, 3);   // older ts from A: entry A stays 5
        assertEntries(clock, 5, 7, 0);
    }

    @Test
    void ownEntryAdvancesOnOldMessage() {
        DirectClock clock = new DirectClock(3, 0);
        clock.tick();
        clock.tick();
        clock.tick();                // own entry is 3
        clock.receiveAction(1, 1);   // ts 1 is behind us
        assertEntries(clock, 4, 1, 0);
    }
}
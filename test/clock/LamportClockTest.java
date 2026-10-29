package clock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LamportClockTest {

    @Test
    void processBMatchesSlide() {
        LamportClock clock = new LamportClock();
        clock.receiveAction(0, 1);   // e_B1: receive from A, ts 1
        assertEquals(2, clock.getValue());
        clock.tick(); // e_B2: send
        assertEquals(3, clock.getValue());
        clock.tick(); // e_B3: local
        assertEquals(4, clock.getValue());
        clock.tick(); // e_B4: send
        assertEquals(5, clock.getValue());
        clock.receiveAction(2, 4); //e_B5: receive from C, ts 4
        assertEquals(6, clock.getValue());
    }
    @Test
    void processAMatchesSlide() {
        LamportClock clock = new LamportClock();
        clock.tick();                // e_A1: send to B
        assertEquals(1, clock.getValue());
        clock.tick();                // e_A2: send to C
        assertEquals(2, clock.getValue());
        clock.receiveAction(1, 3);   // e_A3: receive from B, ts 3
        assertEquals(4, clock.getValue());
        clock.tick();                // e_A4: local
        assertEquals(5, clock.getValue());
        clock.receiveAction(2, 1);   // e_A5: receive from C, ts 1
        assertEquals(6, clock.getValue());
        clock.tick();                // e_A6: local
        assertEquals(7, clock.getValue());
    }

    @Test
    void processCMatchesSlide() {
        LamportClock clock = new LamportClock();
        clock.tick();                // e_C1: send to A
        assertEquals(1, clock.getValue());
        clock.tick();                // e_C2: local
        assertEquals(2, clock.getValue());
        clock.receiveAction(0, 2);   // e_C3: receive from A, ts 2
        assertEquals(3, clock.getValue());
        clock.tick();                // e_C4: send to B
        assertEquals(4, clock.getValue());
        clock.receiveAction(1, 5);   // e_C5: receive from B, ts 5
        assertEquals(6, clock.getValue());
    }
}
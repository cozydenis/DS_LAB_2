package net;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LinkerTest {

    @Test
    @Timeout(5)
    void pointToPointMessageArrives() throws Exception {
        LinkedBlockingQueue<Message> inboxA1 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Message> inboxA2 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Message> inboxA3 = new LinkedBlockingQueue<>();

        Linker a1 = new Linker("A1", 6001, Map.of("A2", 6002, "A3", 6003),
                m -> inboxA1.add(m));
        Linker a2 = new Linker("A2", 6002, Map.of("A1", 6001, "A3", 6003),
                m -> inboxA2.add(m));
        Linker a3 = new Linker("A3", 6003, Map.of("A1", 6001, "A2", 6002),
                m -> inboxA3.add(m));

        a3.start();          // reverse order: higher names only listen,
        a2.start();          // so each start() finds its targets already up
        a1.start();
        a1.awaitConnected();
        a2.awaitConnected();
        a3.awaitConnected();

        a1.send("A2", new Message(MessageType.REQUEST, "A1", 5));

        Message received = inboxA2.poll(1, TimeUnit.SECONDS);
        assertNotNull(received, "A2 received nothing within 1s");
        assertEquals(MessageType.REQUEST, received.getType());
        assertEquals("A1", received.getSender());
        assertEquals(5, received.getTimestamp());
    }

    @Test
    @Timeout(5)
    void broadcast() throws Exception{
        LinkedBlockingQueue<Message> inboxA1 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Message> inboxA2 = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Message> inboxA3 = new LinkedBlockingQueue<>();

        Linker a1 = new Linker("A1", 6011, Map.of("A2", 6012, "A3", 6013),
                m -> inboxA1.add(m));
        Linker a2 = new Linker("A2", 6012, Map.of("A1", 6011, "A3", 6013),
                m -> inboxA2.add(m));
        Linker a3 = new Linker("A3", 6013, Map.of("A1", 6011, "A2", 6012),
                m -> inboxA3.add(m));

        a3.start();          // reverse order: higher names only listen,
        a2.start();          // so each start() finds its targets already up
        a1.start();
        a1.awaitConnected();
        a2.awaitConnected();
        a3.awaitConnected();

        a1.broadcast(List.of("A2", "A3"), new Message(MessageType.REQUEST, "A1", 5));
        Message receivedFromA2 = inboxA2.poll(1, TimeUnit.SECONDS);
        Message receivedFromA3 = inboxA3.poll(1, TimeUnit.SECONDS);

        assertNotNull(receivedFromA2, "A2 received nothing within 1s");
        assertNotNull(receivedFromA3, "A3 received nothing within 1s");

        assertEquals(MessageType.REQUEST, receivedFromA2.getType());
        assertEquals(MessageType.REQUEST, receivedFromA3.getType());

        assertEquals("A1", receivedFromA2.getSender());
        assertEquals("A1", receivedFromA3.getSender());

        assertEquals(5, receivedFromA2.getTimestamp());
        assertEquals(5, receivedFromA3.getTimestamp());
    }
}
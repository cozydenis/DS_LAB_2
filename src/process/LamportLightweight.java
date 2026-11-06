package process;

import clock.DirectClock;
import config.Config;
import net.Message;
import net.MessageType;

import java.util.Arrays;

public class LamportLightweight extends LightWeight<DirectClock> {

    private static final int INF = Integer.MAX_VALUE;   // no request
    private final int[] requestQ = new int[Config.N];
    private int numOkay;

    public LamportLightweight(String name) {
        super(name, new DirectClock(Config.N, Config.indexOf(name)));
        Arrays.fill(requestQ, INF);
    }

    public static void main(String[] args) throws Exception {
        new LamportLightweight(args[0]).run();
    }

    @Override protected synchronized void requestCS() throws InterruptedException {
        clock.tick();
        requestQ[myIndex] = clock.getValue();
        numOkay = 0;
        linker.broadcast(peers, new Message(MessageType.REQUEST, name, requestQ[myIndex]));
        log("REQUEST sent");
        while (!okayCS()) wait();
        log("ENTER CS");
    }
    @Override protected synchronized void releaseCS() {
        clock.tick();
        requestQ[myIndex] = INF;
        linker.broadcast(peers, new Message(MessageType.RELEASE, name, clock.getValue()));
        log("LEAVE CS");
    }
    @Override protected synchronized void handlePeerMsg(Message m) {
        int src = Config.indexOf(m.getSender());
        int ts = m.getTimestamp();
        clock.receiveAction(src, ts);
        log("recv " + m);

        if (m.getType() == MessageType.REQUEST) {
            requestQ[src] = ts;
            linker.send(m.getSender(), new Message(MessageType.ACK, name, clock.getValue()));
        } else if (m.getType() == MessageType.ACK) {
            numOkay++;
        } else if (m.getType() == MessageType.RELEASE) {
            requestQ[src] = INF;
        }
        notifyAll();
    }

    // May I enter? I have all ACKs, nobody is ahead of me in the queue,
    // and I've heard from everyone at a time later than my request.
    private boolean okayCS() {
        if (numOkay < peers.size()) return false;
        for (int j = 0; j < Config.N; j++) {
            if (j == myIndex) continue;
            if (isGreater(requestQ[myIndex], myIndex, requestQ[j], j)) return false;
            if (isGreater(requestQ[myIndex], myIndex, clock.getValue(j), j)) return false;
        }
        return true;
    }

    // Is (entry1, pid1) later than (entry2, pid2)? Ties broken by ID
    private boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        if (entry2 == INF) return false;
        return entry1 > entry2 || (entry1 == entry2 && pid1 > pid2);
    }
}
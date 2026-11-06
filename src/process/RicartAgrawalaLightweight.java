package process;

import clock.LamportClock;
import config.Config;
import net.Message;
import net.MessageType;

import java.util.ArrayList;
import java.util.List;

public class RicartAgrawalaLightweight extends LightWeight<LamportClock> {

    int myTS = Integer.MAX_VALUE;
    int numOkay;
    List<String> pendingQ = new ArrayList<>();

    public RicartAgrawalaLightweight(String name) {
        super(name, new LamportClock());
    }

    public static void main(String[] args) throws Exception {
        new RicartAgrawalaLightweight(args[0]).run();
    }

    // step 6
    @Override protected synchronized void requestCS() throws InterruptedException {
        clock.tick();
        myTS = clock.getValue();
        numOkay = 0;
        linker.broadcast(peers, new Message(MessageType.REQUEST, name, myTS));
        log("REQUEST sent");

        while(numOkay < peers.size()) wait();
        log("ENTER CS");
    }
    // step 6
    @Override protected synchronized void releaseCS() {
        myTS = Integer.MAX_VALUE;
        clock.tick();
        linker.broadcast(pendingQ, new Message(MessageType.REPLY, name, clock.getValue()));
        pendingQ.clear();
        log("LEAVE CS");
    }
    // step 6
    @Override protected synchronized void handlePeerMsg(Message m) {
        int src = Config.indexOf(m.getSender());
        int ts = m.getTimestamp();
        clock.receiveAction(src, ts);
        log("recv " + m);

        if (m.getType() == MessageType.REQUEST) {
            if (myTS == Integer.MAX_VALUE || ts < myTS || (ts == myTS && src < myIndex)) {
                linker.send(m.getSender(), new Message(MessageType.REPLY, name, clock.getValue()));  // permission now
            } else {
                pendingQ.add(m.getSender());                                                       // permission later
            }
        } else if (m.getType() == MessageType.REPLY) {
            numOkay++;
            if (numOkay == peers.size()) notifyAll();
        }
    }
}
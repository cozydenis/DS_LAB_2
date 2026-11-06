package process;

import clock.LogicalClock;
import config.Config;
import net.Linker;
import net.Message;
import net.MessageListener;
import net.MessageType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class LightWeight<C extends LogicalClock> implements MessageListener {
    protected final String name;
    protected final int myIndex;                 // 0–2, for clock + tie-breaks
    protected final String heavyweight;
    protected final List<String> peers;          // for broadcast
    protected final C clock;
    protected Linker linker;
    private final BlockingQueue<Message> fromHeavyweight = new LinkedBlockingQueue<>();
    private static final boolean DEBUG = System.getenv("DS_DEBUG") != null;

    protected LightWeight(String name, C clock) {
        this.name = name;
        this.myIndex = Config.indexOf(name);
        this.heavyweight = Config.heavyweightOf(name);
        this.peers = Config.groupPeersOf(name);
        this.clock = clock;
    }

    @Override
    public void onMessage(Message m) {
        if (m.getType().isAlgorithm()) handlePeerMsg(m);   // REQUEST / ACK / RELEASE / REPLY
        else fromHeavyweight.add(m);                       // START
    }

    public void run() throws Exception {
        linker = new Linker(name, Config.port(name), Config.linkPeers(name), this);
        linker.start();
        linker.awaitConnected();

        linker.send(heavyweight, new Message(MessageType.READY, name));

        while (true){
            while (fromHeavyweight.take().getType() != MessageType.START){ }
            requestCS();
            for(int i = 0; i<10; i++){
                System.out.println("I'm lightweight process " + name);
                Thread.sleep(1000);
            }
            releaseCS();
            linker.send(heavyweight, new Message(MessageType.DONE, name));
        }

    }

    protected void log(String event) {
        if (DEBUG) System.err.println("[" + name + " clock=" + clock.getValue() + "] " + event);
    }

    protected abstract void requestCS() throws InterruptedException;
    protected abstract void releaseCS();
    protected abstract void handlePeerMsg(Message m);     // synchronized in the subclasses
}
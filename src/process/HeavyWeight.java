package process;

import config.Config;
import net.Linker;
import net.Message;
import net.MessageListener;
import net.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HeavyWeight implements MessageListener {

    private final String name;
    private final BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    private Linker linker;
    private final List<Process> children = new ArrayList<>();

    public HeavyWeight(String name) {
        this.name = name;
    }

    public static void main(String[] args) throws Exception{
        HeavyWeight process = new HeavyWeight(args[0]);
        process.run();
        // args[0] = "A" or "B"
    }
    public void onMessage(Message m){
        inbox.add(m);
    }
    public void run() throws Exception{
        String next = Config.nextHeavyweight(name);
        List<String> childNames = Config.childrenOf(name);
        
        spawnChildren(childNames);

        linker = new Linker(name, Config.port(name), Config.linkPeers(name), this);
        linker.start();
        linker.awaitConnected();

        boolean token = name.equals(Config.FIRST_TOKEN_HOLDER);

        //wait until all children are READY
        int ready = 0;
        while (ready < childNames.size()) {
            Message m = inbox.take();
            if (m.getType() == MessageType.READY) ready++;
            else if (m.getType() == MessageType.TOKEN) token = true;   // don't lose an early token
        }

        while(true){
            while(!token){ //wait until token arrives
                Message m = inbox.take();
                if (m.getType() == MessageType.TOKEN) token = true;
            }

            linker.broadcast(childNames, new Message(MessageType.START, name));
            int done = 0;
            while (done < childNames.size()) {
                if (inbox.take().getType() == MessageType.DONE) done++;
            }
            token = false;
            linker.send(next, new Message(MessageType.TOKEN, name));   // pass it on
        }
    }

    private void spawnChildren(List<String> childNames) throws IOException {
        String java = System.getProperty("java.home") + "/bin/java";
        String cp = System.getProperty("java.class.path");
        String childClass = name.equals("A") ? "process.LamportLightweight"
                : "process.RicartAgrawalaLightweight";
        for (String child : childNames) {
            children.add(new ProcessBuilder(java, "-cp", cp, childClass, child).inheritIO().start());
        }
        // kill the children when this heavyweight stops, so they don't keep the ports
        Runtime.getRuntime().addShutdownHook(new Thread(() -> children.forEach(Process::destroy)));
    }

}

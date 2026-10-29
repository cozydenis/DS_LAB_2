package net;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Linker {
    private final String myName;
    private final int myPort;
    private final Map<String, Integer> peers;            // name → port of everyone I talk to
    private final MessageListener listener;
    private final Map<String, PrintWriter> writers = new ConcurrentHashMap<>();
    private final CountDownLatch connected;              // initialised with peers.size()

    public Linker(String myName, int myPort, Map<String, Integer> peers, MessageListener listener){
        //TODO
    }

    // open server, start accept thread, connect outward
    public void start(){
        //TODO
    }
    public void awaitConnected()                         // block until all peers registered
    public void send(String dest, Message m)
    public void broadcast(Collection<String> dests, Message m)

    private void acceptLoop(ServerSocket server)         // accept → read HELLO → register
    private Socket connectWithRetry(int port)
    private void register(String name, Socket socket)    // create writer + reader thread, countDown()
    private void readLoop(String name, BufferedReader in)
}
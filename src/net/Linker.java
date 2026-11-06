package net;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;

public class Linker {
    private final String myName;
    private final int myPort;
    private final Map<String, Integer> peers;            // name → port of everyone I talk to
    private final MessageListener listener;
    private final Map<String, PrintWriter> writers = new ConcurrentHashMap<>();
    private final CountDownLatch connected;              // initialised with peers.size()

    public Linker(String myName, int myPort, Map<String, Integer> peers, MessageListener listener){
        this.myName = myName;
        this.myPort = myPort;
        this.peers = peers;
        this.listener = listener;
        this.connected = new CountDownLatch(peers.size());
    }

    // open server, start accept thread, connect outward
    public void start() throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(myPort);
        new Thread(() -> acceptLoop(server), "acceptor-" + myName).start();
        for(Map.Entry<String, Integer>peer : peers.entrySet()){
            String peerName = peer.getKey();
            Integer peerPort = peer.getValue();

            if(peerName.compareTo(myName) <= 0) continue;

            Socket socket = connectWithRetry(peerPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // introduce ourselves, so the acceptor knows who we are
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(new Message(MessageType.HELLO, myName).toLine());

            register(peerName, socket, reader);
        }


    }
    // block until all peers registered
    public void awaitConnected() throws InterruptedException {
        connected.await();

    }
    public void send(String dest, Message m){
        PrintWriter writer = writers.get(dest);
        if (writer == null) throw new IllegalStateException(myName + " has no connection to " + dest);
        synchronized (writer) { writer.println(m.toLine());}
    }
    public void broadcast(Collection<String> dests, Message m){
        for(String dest: dests){
            send(dest, m);
        }
    }

    // accept → read HELLO → register
    private void acceptLoop(ServerSocket server){
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    // create reader, read + check HELLO, register
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String first = reader.readLine();
                    if(first == null){
                        socket.close();
                        continue;
                    }
                    Message hello = Message.parse(first);
                    if (hello.getType() == MessageType.HELLO) {
                        register(hello.getSender(), socket, reader);
                    }else {
                        System.err.println("expected HELLO, got: " + first);
                        socket.close();
                    }
                } catch (IOException | IllegalArgumentException e) {
                    // log it; this one connection is lost, the loop continues
                    System.err.println("rejected incoming connection on " + myName + ": " + e.getMessage());
                    try { socket.close(); } catch (IOException ignored) {
                        System.err.println("ignored: " + e.getMessage());
                    }
                }
                }
            } catch (IOException e) {
            // accept() itself failed → the server socket is gone; log and return
            System.err.println("acceptor for " + myName + " stopped: " + e.getMessage());

        }
    }
    private Socket connectWithRetry(int port) throws IOException, InterruptedException {
        while (true) {
            try {
                return new Socket("localhost", port);
            } catch (ConnectException e) {
                Thread.sleep(100);
            }
        }

    }

    // create writer + reader thread, countDown()
    private void register(String name, Socket socket, BufferedReader in) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writers.put(name, writer);

        // read peer's messages on a seperate thread
        new Thread(() -> readLoop(name, in), "reader-" + name).start();

        // mark peer as connected
        connected.countDown();
    }
    private void readLoop(String name, BufferedReader in){
        String line;
        try {
            while((line = in.readLine()) != null){
                try {
                    listener.onMessage(Message.parse(line));
                } catch (IllegalArgumentException e) {
                    System.err.println("bad message from " + name + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("connection to " + name + " broken: " + e.getMessage());

        }
        System.err.println("peer " + name + " disconnected");
    }
}
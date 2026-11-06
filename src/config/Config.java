package config;

import java.util.*;

public final class Config {
    private Config() {}

    public static final int N = 3;              // lightweights per group
    public static final String FIRST_TOKEN_HOLDER = "A";
    public static final List<String> HEAVYWEIGHTS = List.of("A", "B");

    private static final Map<String, Integer> PORTS = Map.of(
            "A", 6100, "B", 6101,
            "A1", 6111, "A2", 6112, "A3", 6113,
            "B1", 6121, "B2", 6122, "B3", 6123);

    public static int port(String name){
        Integer port = PORTS.get(name);
        if (port == null) throw new IllegalArgumentException("unknown process: " + name);
        return port;

    }
    public static int indexOf(String lw){
        port(lw);
        if (lw.length() != 2) throw new IllegalArgumentException("not a lightweight: " + lw);
        return lw.charAt(1) - '1'; // "B1" → 0, "B2" → 1, "B3" → 2
    }
    public static String heavyweightOf(String lw){
        port(lw);
        if (HEAVYWEIGHTS.contains(lw)) throw new IllegalArgumentException("not a lightweight: " + lw);
        return lw.substring(0,1); // "B2" → "B"
    }
    public static String nextHeavyweight(String hw){
        int i = HEAVYWEIGHTS.indexOf(hw);
        if (i == -1) throw new IllegalArgumentException("not a heavyweight: " + hw);
        return HEAVYWEIGHTS.get((i + 1) % HEAVYWEIGHTS.size());
    }
    public static List<String> childrenOf(String hw){
        if(!HEAVYWEIGHTS.contains(hw)) throw new IllegalArgumentException("not a heavyweight: " + hw);

        List<String> children = new ArrayList<>();
        for(int i=1; i <= N; i++){
            children.add(hw + i);
        }
        return children;

    }
    public static List<String> groupPeersOf(String lw){
        List<String> peers = childrenOf(heavyweightOf(lw));
        peers.remove(lw);
        return peers;
    }
    public static Map<String, Integer> linkPeers(String name){
        HashMap<String, Integer> linkedPeers = new HashMap<>();
        if(HEAVYWEIGHTS.contains(name)){
            List<String> children = childrenOf(name);
            linkedPeers.put(nextHeavyweight(name), port(nextHeavyweight(name)));

            for(String child: children){
                linkedPeers.put(child, port(child));
            }
        }else{
            List<String> peers = groupPeersOf(name);
            peers.add(heavyweightOf(name));
            for(String peer: peers){
                linkedPeers.put(peer, port(peer));
            }
        }
        return linkedPeers;
    }
}

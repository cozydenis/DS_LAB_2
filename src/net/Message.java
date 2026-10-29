package net;

public final class Message {
    private final MessageType type;
    private final String sender;
    private final int timestamp;

    public Message(MessageType type, String sender, int timestamp){
        this.type = type;
        this.sender = sender;
        this.timestamp = timestamp;
    }
    public Message(MessageType type, String sender){
        this(type, sender, -1);
    }

    public MessageType getType(){
        return this.type;
    }

    public String getSender(){
        return this.sender;
    }

    public int getTimestamp(){
        return this.timestamp;
    }

    public String toLine(){
        return type.name() + " " + sender + " " + timestamp;
    }

    @Override
    public String toString() {
        return toLine();
    }

    public static Message parse(String line){
        String regex = "\\s+";
        String[] p = line.trim().split(regex);
        if (p.length != 3){
            throw new IllegalArgumentException();
        }
        return new Message(MessageType.valueOf(p[0]), p[1], Integer.parseInt(p[2]));
    }
}

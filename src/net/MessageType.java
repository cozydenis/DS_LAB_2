package net;


public enum MessageType {
    HELLO(false), READY(false), START(false), DONE(false), TOKEN(false),
    REQUEST(true), ACK(true), RELEASE(true), REPLY(true);

    //is this message part of the mutual exclusion algorithm,
    // and does it therefore carry a logical-clock timestamp?
    private final boolean algorithm;

    MessageType(boolean algorithm){
        this.algorithm = algorithm;
    }

    public boolean isAlgorithm(){
        return algorithm;
    }


}

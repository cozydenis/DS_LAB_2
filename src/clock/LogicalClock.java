package clock;

public interface LogicalClock {

    void tick();
    void receiveAction(int src, int ts);
    int getValue();
}

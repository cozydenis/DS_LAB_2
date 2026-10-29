package clock;



public class DirectClock implements LogicalClock {
    private final int[] clock;
    private final int myId;

    public DirectClock(int size, int id){
        this.myId = id;
        clock = new int[size];
    }
    @Override
    public void tick() {
        clock[myId]++;
    }

    @Override
    public void receiveAction(int sender, int ts) {
        clock[sender] = Math.max(clock[sender], ts);
        clock[myId] = Math.max(clock[myId], ts) + 1;
    }

    @Override
    public int getValue() { return clock[myId]; }

    public int getValue(int i) { return clock[i]; } //extra for DirectCLock
}

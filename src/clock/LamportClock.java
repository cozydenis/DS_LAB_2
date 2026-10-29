package clock;


public class LamportClock implements LogicalClock {
    private int clock = 0;

    @Override
    public void tick() { this.clock++; }

    @Override
    public void receiveAction(int src, int ts){
        clock = Math.max(this.clock, ts) + 1;
    }
    @Override
    public int getValue(){
      return this.clock;
    }

}

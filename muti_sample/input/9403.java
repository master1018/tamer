public class CountedTimerTask extends TimerTask {
    volatile long executionCount;
    public long executionCount() {
        return executionCount;
    }
    public void run() {
        executionCount++;
    }
}

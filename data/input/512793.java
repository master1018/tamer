public class LowLevelNetRunner extends Thread {
    private int count = 0;
    LowLevelNetRunner() {
    }
    public void incrementRunCount() {
        count++;
    }
    public void decrementRunCount() {
        count--;
        if (count <= 0) {
            synchronized (RequestAPITest.syncObj) {
                RequestAPITest.syncObj.notifyAll();
            }
        }
    }
} 

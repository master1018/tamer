public class WaitingThreadAborter {
    private WaitingThread waitingThread;
    private boolean aborted;
    public void abort() {
        aborted = true;
        if (waitingThread != null)
            waitingThread.interrupt();
    }
    public void setWaitingThread(WaitingThread waitingThread) {
        this.waitingThread = waitingThread;
        if (aborted)
            waitingThread.interrupt();
    }
}

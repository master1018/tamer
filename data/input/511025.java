public class ProgressObserver {
    private Timer mNotifyTimer;
    public void start() {
        mNotifyTimer = new Timer();
        mNotifyTimer.schedule(new ProgressPrinter(),
                ProgressPrinter.DELAY, ProgressPrinter.TIMEOUT);
    }
    public void stop() {
        if (mNotifyTimer != null) {
            mNotifyTimer.cancel();
        }
        mNotifyTimer = null;
    }
    class ProgressPrinter extends TimerTask {
        public final static int DELAY = 2000;
        public final static int TIMEOUT = 2000;
        @Override
        public void run() {
            CUIOutputStream.print(".");
        }
    }
}

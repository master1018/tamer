public final class ShutdownThread extends Thread {
    public static final class Watchdog {
    }
    public ShutdownThread() {
        setName("AWT-Shutdown"); 
        setDaemon(false);
    }
    private boolean shouldStop = false;
    @Override
    public void run() {
        synchronized (this) {
            notifyAll(); 
            while (true) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                if (shouldStop) {
                    notifyAll(); 
                    return;
                }
            }
        }
    }
    @Override
    public void start() {
        synchronized (this) {
            super.start();
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(
                        Messages.getString("awt.26")); 
            }
        }
    }
    public void shutdown() {
        synchronized (this) {
            shouldStop = true;
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(
                        Messages.getString("awt.27")); 
            }
        }
    }
}

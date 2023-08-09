public class HoldsLock {
    private static Object target = null;
    private static void checkLock(boolean value) {
        if (Thread.holdsLock(target) != value)
            throw new RuntimeException("Should be " + value);
    }
    static class LockThread extends Thread {
        public void run() {
            checkLock(false);
            synchronized(target) {
                checkLock(true);
            }
            checkLock(false);
        }
    }
    public static void main(String args[]) throws Exception {
        try {
            checkLock(false);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException e) {
        };
        target = new Object();
        checkLock(false);
        synchronized(target) {
            checkLock(true);
        }
        checkLock(false);
        synchronized(target) {
            checkLock(true);
            new LockThread().start();
            checkLock(true);
            Thread.sleep(100);
            checkLock(true);
        }
        checkLock(false);
    }
}

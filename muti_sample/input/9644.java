public class Attack implements Runnable {
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean failedDueToSecurityException;
    public void Attack() {
        if (Attack.class.getClassLoader() != null)
            throw new RuntimeException("Attack class not on boot class path");
    }
    @Override
    public void run() {
        try {
            new Socket("127.0.0.1", 9999).close();
            throw new RuntimeException("Connected (not expected)");
        } catch (IOException e) {
            throw new RuntimeException("IOException (not expected)");
        } catch (SecurityException e) {
            failedDueToSecurityException = true;
        } finally {
            latch.countDown();
        }
    }
    public void waitUntilDone() throws InterruptedException {
        latch.await();
    }
    public boolean failedDueToSecurityException() {
        return failedDueToSecurityException;
    }
}

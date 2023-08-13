public class NullThreadName
{
    static CountDownLatch done = new CountDownLatch(1);
    public static void main(String args[]) throws Exception {
        ThreadGroup tg = new ThreadGroup("chegar-threads");
        Thread goodThread = new Thread(tg, new GoodThread(), "goodThread");
        try {
            Thread badThread = new Thread(tg, new Runnable(){
                @Override
                public void run() {} }, null);
        } catch (NullPointerException npe) {
            out.println("OK, caught expected " + npe);
        }
        tg.setDaemon(true);
        goodThread.start();
        done.await();
        int count = 0;
        while (goodThread.isAlive()) {
            out.println("GoodThread still alive, sleeping...");
            try { Thread.sleep(2000); }
            catch (InterruptedException unused) {}
            if (count++ > 5)
                throw new AssertionError("GoodThread is still alive!");
        }
        if (!tg.isDestroyed()) {
            throw new AssertionError("Failed: Thread group is not destroyed.");
        }
    }
    static class GoodThread implements Runnable
    {
        @Override
        public void run() {
            out.println("Good Thread started...");
            out.println("Good Thread finishing");
            done.countDown();
        }
    }
}

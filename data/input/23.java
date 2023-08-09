public class WakeupAfterClose {
    public static void main(String[] args) throws Exception {
        final Selector sel = Selector.open();
        Runnable r = new Runnable() {
            public void run() {
                try {
                    sel.select();
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        Thread.sleep(1000);
        t.interrupt();
        sel.close();
        sel.wakeup();
    }
}

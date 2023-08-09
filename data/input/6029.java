public class WakeupEmpty {
    private final static int SLEEP_TIME = 100;
    public static void main(String[] argv) throws Exception {
        final Selector sel = Selector.open();
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                }
                sel.wakeup();
            }
        };
        thread.start();
        if (sel.select() != 0)
             throw new Exception("Zero expected");
    }
}

public class bug6432565 {
    private final static AtomicReference<Throwable> throwable =
        new AtomicReference<Throwable>(null);
    private final static AtomicBoolean isDone = new AtomicBoolean(false);
    public static void main(String[] args) throws Exception {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventProcessor());
        SwingWorker<Void, CharSequence> swingWorker =
            new SwingWorker<Void,CharSequence>() {
                @Override
                protected Void doInBackground() {
                    publish(new String[] {"hello"});
                    publish(new StringBuilder("world"));
                    return null;
                }
                @Override
                protected void done() {
                    isDone.set(true);
                }
            };
        swingWorker.execute();
        while (! isDone.get()) {
            Thread.sleep(100);
        }
        if (throwable.get() instanceof ArrayStoreException) {
            throw new RuntimeException("Test failed");
        }
    }
    private final static class EventProcessor extends EventQueue {
        @Override
        protected void dispatchEvent(AWTEvent event) {
            try {
                super.dispatchEvent(event);
            } catch (Throwable e) {
                e.printStackTrace();
                throwable.set(e);
            }
        }
    }
}

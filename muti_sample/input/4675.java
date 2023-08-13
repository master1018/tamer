public class bug6493680 {
    private final static int NUMBER_OF_TRIES = 50;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < NUMBER_OF_TRIES; i++) {
            if (! (new Test()).test()) {
                throw new RuntimeException("failed");
            }
        }
    }
    private static class Test {
        private final AtomicInteger lastProgressValue = new AtomicInteger(-1);
        private final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        boolean test() throws Exception {
            TestSwingWorker swingWorker = new TestSwingWorker();
            swingWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress" == evt.getPropertyName()) {
                            lastProgressValue.set((Integer) evt.getNewValue());
                        }
                    }
                });
            swingWorker.execute();
            return exchanger.exchange(true);
        }
        private class TestSwingWorker extends SwingWorker<Void, Void> {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(1);
                    setProgress(i);
                }
                return null;
            }
            @Override
            protected void done() {
                boolean isPassed = (lastProgressValue.get() == 100);
                try {
                    exchanger.exchange(isPassed);
                } catch (Exception ingore) {
                }
            }
        }
    }
}

public class NestedWorkers extends SwingWorker<String, Void> {
    private final static int MAX_LEVEL = 2;
    private int level;
    public NestedWorkers(int level) {
        super();
        this.level = level;
    }
    @Override
    public String doInBackground() throws Exception {
        if (level < MAX_LEVEL) {
            SwingWorker<String, Void> nested = new NestedWorkers(level + 1);
            nested.execute();
            nested.get();
        }
        System.out.println("doInBackground " + level + " is complete");
        return String.valueOf(level);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                SwingWorker<String, Void> sw = new NestedWorkers(0);
                sw.execute();
                try {
                    System.err.println(sw.get());
                } catch (Exception z) {
                    throw new RuntimeException(z);
                }
            }
        });
    }
}

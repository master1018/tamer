public class Test4177735 implements Runnable {
    private static final long DELAY = 1000L;
    public static void main(String[] args) throws Exception {
        JColorChooser chooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        chooser.setChooserPanels(new AbstractColorChooserPanel[] { panels[1] });
        JDialog dialog = show(chooser);
        pause(DELAY);
        dialog.dispose();
        pause(DELAY);
        Test4177735 test = new Test4177735();
        SwingUtilities.invokeAndWait(test);
        if (test.count != 0) {
            throw new Error("JColorChooser leaves " + test.count + " threads running");
        }
    }
    static JDialog show(JColorChooser chooser) {
        JDialog dialog = JColorChooser.createDialog(null, null, false, chooser, null, null);
        dialog.setVisible(true);
        Point point = null;
        while (point == null) {
            try {
                point = dialog.getLocationOnScreen();
            }
            catch (IllegalStateException exception) {
                pause(DELAY);
            }
        }
        return dialog;
    }
    private static void pause(long delay) {
        try {
            Thread.sleep(delay);
        }
        catch (InterruptedException exception) {
        }
    }
    private int count;
    public void run() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[group.activeCount()];
        int count = group.enumerate(threads, false);
        for (int i = 0; i < count; i++) {
            String name = threads[i].getName();
            if ("SyntheticImageGenerator".equals(name)) { 
                this.count++;
            }
        }
    }
}

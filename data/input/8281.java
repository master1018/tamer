public class bug6520101 implements Runnable {
    private static final int ATTEMPTS = 500;
    private static final int INTERVAL = 100;
    private static final boolean ALWAYS_NEW_INSTANCE = false;
    private static final boolean DO_GC_EACH_INTERVAL = false;
    private static final boolean UPDATE_UI_EACH_INTERVAL = true;
    private static final boolean AUTO_CLOSE_DIALOG = true;
    private static JFileChooser CHOOSER;
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        for (int i = 0; i < ATTEMPTS; i++) {
            doAttempt();
        }
        System.out.println("Test passed successfully");
    }
    private static void doAttempt() throws InterruptedException {
        if (ALWAYS_NEW_INSTANCE || (CHOOSER == null))
            CHOOSER = new JFileChooser(".");
        if (UPDATE_UI_EACH_INTERVAL) {
            CHOOSER.updateUI();
        }
        if (AUTO_CLOSE_DIALOG) {
            Thread t = new Thread(new bug6520101(CHOOSER));
            t.start();
            CHOOSER.showOpenDialog(null);
            t.join();
        } else {
            CHOOSER.showOpenDialog(null);
        }
        if (DO_GC_EACH_INTERVAL) {
            System.gc();
        }
    }
    private final JFileChooser chooser;
    bug6520101(JFileChooser chooser) {
        this.chooser = chooser;
    }
    public void run() {
        while (!this.chooser.isShowing()) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        Timer timer = new Timer(INTERVAL, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooser.cancelSelection();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}

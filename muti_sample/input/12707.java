public class Test6981576 extends TitledBorder implements Runnable, Thread.UncaughtExceptionHandler {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Test6981576());
    }
    private int index;
    private LookAndFeelInfo[] infos;
    private JFrame frame;
    private Test6981576() {
        super("");
    }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        getBorder().paintBorder(c, g, x, y, width, height);
    }
    public void run() {
        if (this.infos == null) {
            this.infos = UIManager.getInstalledLookAndFeels();
            Thread.currentThread().setUncaughtExceptionHandler(this);
            JPanel panel = new JPanel();
            panel.setBorder(this);
            this.frame = new JFrame(getClass().getSimpleName());
            this.frame.add(panel);
            this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.frame.setVisible(true);
        }
        if (this.index == this.infos.length) {
            this.frame.dispose();
        }
        else {
            LookAndFeelInfo info = this.infos[this.index % this.infos.length];
            try {
                UIManager.setLookAndFeel(info.getClassName());
            }
            catch (Exception exception) {
                System.err.println("could not change look and feel");
            }
            SwingUtilities.updateComponentTreeUI(this.frame);
            this.frame.pack();
            this.frame.setLocationRelativeTo(null);
            this.index++;
            SwingUtilities.invokeLater(this);
        }
    }
    public void uncaughtException(Thread thread, Throwable throwable) {
        System.exit(1);
    }
}

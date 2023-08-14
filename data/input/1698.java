public class Test6919629
{
    JFrame f;
    WeakReference<JLabel> ref;
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        Test6919629 t = new Test6919629();
        t.test();
        System.gc();
        t.check();
    }
    void test() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                UIDefaults d = new UIDefaults();
                d.put("Label.textForeground", Color.MAGENTA);
                JLabel l = new JLabel();
                ref = new WeakReference<JLabel>(l);
                l.putClientProperty("Nimbus.Overrides", d);
                f = new JFrame();
                f.getContentPane().add(l);
                f.pack();
                f.setVisible(true);
            }
        });
        Thread.sleep(2000);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                f.getContentPane().removeAll();
                f.setVisible(false);
                f.dispose();
            }
        });
        Thread.sleep(2000);
    }
    void check() {
        if (ref.get() != null) {
            throw new RuntimeException("Failed: an unused component wasn't collected");
        }
    }
}

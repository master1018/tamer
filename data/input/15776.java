public class Test6635110 implements Runnable {
    static final int WIDTH = 160;
    static final int HEIGHT = 80;
    final BufferedImage IMAGE =
            new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
    @Override public void run() {
        JMenu menu = new JMenu("menu");
        menu.setUI(new BasicMenuUI());
        paint(menu);
        JToolBar tb = new JToolBar();
        tb.setFloatable(true);
        tb.setUI(new BasicToolBarUI());
        paint(tb);
    }
    void paint(Component c) {
        c.setSize(WIDTH, HEIGHT);
        c.paint(IMAGE.getGraphics());
    }
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            System.out.println("GTKLookAndFeel cannot be set, skipping this test");
            return;
        }
        SwingUtilities.invokeAndWait(new Test6635110());
    }
}

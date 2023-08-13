public class Test6741426 implements Runnable {
    static final int WIDTH = 160;
    static final int HEIGHT = 80;
    @Override public void run() {
        JComboBox cb = new JComboBox();
        JTextField tf = new JTextField();
        tf.setBorder(cb.getBorder());
        BufferedImage img =
                new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        tf.setSize(WIDTH, HEIGHT);
        tf.paint(img.getGraphics());
    }
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        SwingUtilities.invokeAndWait(new Test6741426());
    }
}

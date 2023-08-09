public class bug6797139 {
    private static void createGui() {
        JButton b = new JButton("Probably");
        b.setUI(new BasicButtonUI() {
            protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
                super.paintText(g, b, textRect, text);
                if (text.endsWith("...")) {
                    throw new RuntimeException("Text is truncated!");
                }
            }
        });
        b.setSize(b.getPreferredSize());
        BufferedImage image = new BufferedImage(b.getWidth(), b.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        b.paint(g);
        g.dispose();
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                createGui();
            }
        });
    }
}

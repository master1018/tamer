public class bug6604281 {
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                SynthLookAndFeel laf = new SynthLookAndFeel();
                try {
                    UIManager.setLookAndFeel(laf);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = (Graphics2D) image.getGraphics();
                graphics.setColor(Color.BLUE);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                graphics.setColor(Color.RED);
                graphics.drawLine(0, 0, image.getWidth(), image.getHeight());
                JButton button1 = new JButton(new IconUIResource(new ImageIcon(image)));
                JButton button2 = new JButton(new IconUIResource(new ImageIcon(image)));
                button2.setEnabled(false);
                if (button1.getPreferredSize().getHeight() != button2.getPreferredSize().getHeight()) {
                    fail("Two similar buttons have different size");
                }
            }
        });
    }
    private static void fail(String s) {
        throw new RuntimeException("Test failed: " + s);
    }
}

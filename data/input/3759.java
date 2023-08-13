public class Test4783068 {
    final static Color TEST_COLOR = Color.WHITE;
    final static String html = "<html>" +
                  "This is a <font color='red'>colored</font> <b>text</b>" +
                  "<p>with a <a href='http:
                  "<ul><li>an unordered<li>list</ul>" +
                  "<ol><li>and an ordered<li>list</ol>" +
                  "</html>";
    void test() {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new Error("Cannot set Metal LAF");
        }
        UIManager.put("textInactiveText", TEST_COLOR);
        test(new JLabel(html));
        test(new JButton(html));
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setDisabledTextColor(TEST_COLOR);
        test(pane);
    }
    void test(JComponent c) {
        c.setEnabled(false);
        c.setOpaque(true);
        c.setBackground(TEST_COLOR);
        c.setBorder(null);
        Dimension size = c.getPreferredSize();
        c.setBounds(0, 0, size.width, size.height);
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        c.paint(image.getGraphics());
        int rgb = TEST_COLOR.getRGB();
        for (int i = 0; i < size.height; i++) {
            for (int j = 0; j < size.width; j++) {
                if (image.getRGB(j, i) != rgb) {
                    throw new RuntimeException(
                            String.format("Color mismatch at [%d, %d]", j, i));
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                new Test4783068().test();
            }
        });
    }
}

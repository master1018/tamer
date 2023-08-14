public class bug6742358 extends JApplet {
    public static void main(String[] args) {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        JFrame frame = new JFrame();
        frame.setContentPane(new TestPanel());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public void init() {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        TestPanel panel = new TestPanel();
        setContentPane(panel);
    }
    private static class TestPanel extends JPanel {
        private TestPanel() {
            JPanel pnVertical = new JPanel();
            pnVertical.setLayout(new BoxLayout(pnVertical, BoxLayout.Y_AXIS));
            for (int i = 0; i < 8; i++) {
                pnVertical.add(createSlider(false, (i & 4) == 0, (i & 2) == 0, (i & 1) == 0));
            }
            JPanel pnHorizontal = new JPanel();
            pnHorizontal.setLayout(new BoxLayout(pnHorizontal, BoxLayout.X_AXIS));
            for (int i = 0; i < 8; i++) {
                pnHorizontal.add(createSlider(true, (i & 4) == 0, (i & 2) == 0, (i & 1) == 0));
            }
            add(pnHorizontal);
            add(pnVertical);
        }
    }
    private static JSlider createSlider(boolean vertical, boolean enabled, boolean filled, boolean inverted) {
        JSlider result = new JSlider(vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL, 0, 10, 5);
        result.setEnabled(enabled);
        result.putClientProperty("JSlider.isFilled", filled);
        result.setInverted(inverted);
        result.setToolTipText("<html>vertical = " + vertical + "<br>enabled = " + enabled + "<br>filled = " + filled +
                "<br>inverted = " + inverted + "</html>");
        return result;
    }
}

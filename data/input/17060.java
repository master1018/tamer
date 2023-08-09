public class bug6524424 extends JApplet {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
            return;
        }
        TestPanel panel = new TestPanel();
        JFrame frame = new JFrame();
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public void init() {
        TestPanel panel = new TestPanel();
        setContentPane(panel);
    }
    private static class TestPanel extends JPanel {
        private TestPanel() {
            super(new GridBagLayout());
            JSlider slider1 = createSlider(1, 2);
            JSlider slider2 = createSlider(2, 4);
            JSlider slider3 = createSlider(3, 6);
            addComponent(this, slider1);
            addComponent(this, slider2);
            addComponent(this, slider3);
        }
        private JSlider createSlider(int tickMinor, int tickMajor) {
            JSlider result = new JSlider();
            result.setPaintLabels(true);
            result.setPaintTicks(true);
            result.setSnapToTicks(true);
            result.setMinimum(0);
            result.setMaximum(12);
            result.setMinorTickSpacing(tickMinor);
            result.setMajorTickSpacing(tickMajor);
            return result;
        }
    }
    private static void addComponent(JPanel panel, Component component) {
        panel.add(component, new GridBagConstraints(0,
                panel.getComponentCount(), 1, 1,
                1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
    }
}

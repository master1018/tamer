public class bug6587742 extends JApplet {
    public void init() {
        TestPanel panel = new TestPanel();
        setContentPane(panel);
    }
    private class TestPanel extends JPanel {
        private final JComboBox cbThemes = new JComboBox();
        private TestPanel() {
            cbThemes.addItem(new OceanTheme());
            cbThemes.addItem(new DefaultMetalTheme());
            cbThemes.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    MetalTheme theme = (MetalTheme) cbThemes.getSelectedItem();
                    if (theme != null) {
                        MetalLookAndFeel.setCurrentTheme(theme);
                        try {
                            UIManager.setLookAndFeel(new MetalLookAndFeel());
                        } catch (UnsupportedLookAndFeelException e1) {
                            JOptionPane.showMessageDialog(TestPanel.this, "Can't change theme: " + e1.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        SwingUtilities.updateComponentTreeUI(bug6587742.this);
                    }
                }
            });
            JPanel pnVertical = new JPanel();
            pnVertical.setLayout(new BoxLayout(pnVertical, BoxLayout.Y_AXIS));
            for (int i = 0; i < 12; i++) {
                int filled = i >> 2;
                pnVertical.add(createSlider(false, filled > 1 ? null : Boolean.valueOf(filled == 1), (i & 2) == 0,
                        (i & 1) != 0));
            }
            JPanel pnHorizontal = new JPanel();
            pnHorizontal.setLayout(new BoxLayout(pnHorizontal, BoxLayout.X_AXIS));
            for (int i = 0; i < 12; i++) {
                int filled = i >> 2;
                pnHorizontal.add(createSlider(true, filled > 1 ? null : Boolean.valueOf(filled == 1), (i & 2) == 0,
                        (i & 1) != 0));
            }
            JTabbedPane tpSliders = new JTabbedPane();
            tpSliders.add("Vertical sliders", pnVertical);
            tpSliders.add("Horizontal sliders", pnHorizontal);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new JLabel("Select theme:"));
            add(cbThemes);
            add(tpSliders);
        }
    }
    private static JSlider createSlider(boolean vertical, Boolean filled, boolean enabled, boolean inverted) {
        JSlider result = new JSlider(vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL, 0, 100, 50);
        result.setMajorTickSpacing(20);
        result.setMinorTickSpacing(5);
        result.setPaintTicks(true);
        result.setPaintLabels(true);
        result.setEnabled(enabled);
        if (filled != null) {
            result.putClientProperty("JSlider.isFilled", filled);
        }
        result.setInverted(inverted);
        result.setToolTipText("<html>vertical = " + vertical + "<br>enabled = " + enabled + "<br>filled = " + filled +
                "<br>inverted = " + inverted + "</html>");
        return result;
    }
}

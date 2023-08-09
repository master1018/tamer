public class ComponentTest extends JFrame {
    private static JFrame frame;
    public ComponentTest() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        add(new JButton("JButton"));
        add(new JToggleButton("JToggleButton"));
        add(new JCheckBox("JCheckBox"));
        add(new JRadioButton("JRadioButton"));
        add(new JLabel("JLabel"));
        pack();
        setLocationRelativeTo(null);
    }
    public static void main(String[] args) throws Exception {
        SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame = new ComponentTest();
                frame.setVisible(true);
            }
        });
        toolkit.realSync();
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (final UIManager.LookAndFeelInfo laf : lafs) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel(laf.getClassName());
                    } catch (Exception e) {
                        new RuntimeException(e);
                    }
                    SwingUtilities.updateComponentTreeUI(frame);
                }
            });
            toolkit.realSync();
        }
    }
}

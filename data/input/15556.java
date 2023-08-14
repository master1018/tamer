public class bug6596966 {
    private static JFrame frame;
    private static JLabel label;
    private static JButton button;
    private static JComboBox comboBox;
    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        SunToolkit toolkit = (SunToolkit) SunToolkit.getDefaultToolkit();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                button = new JButton("Button");
                comboBox = new JComboBox();
                label = new JLabel("Label");
                label.setDisplayedMnemonic('L');
                label.setLabelFor(comboBox);
                JPanel pnContent = new JPanel();
                pnContent.add(button);
                pnContent.add(label);
                pnContent.add(comboBox);
                frame = new JFrame();
                frame.add(pnContent);
                frame.pack();
                frame.setVisible(true);
            }
        });
        toolkit.realSync();
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_L);
        toolkit.realSync();
        toolkit.getSystemEventQueue().postEvent(new KeyEvent(label, KeyEvent.KEY_RELEASED,
                EventQueue.getMostRecentEventTime(), 0, KeyEvent.VK_L, 'L'));
        toolkit.realSync();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if (!comboBox.isFocusOwner()) {
                        throw new RuntimeException("comboBox isn't focus owner");
                    }
                }
            });
        } finally {
            robot.keyRelease(KeyEvent.VK_ALT);
        }
    }
}

public class bug6940863 {
    private static JFrame frame;
    private static JScrollPane scrollPane;
    private static final Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            boolean failed = scrollPane.getVerticalScrollBar().isShowing() ||
                    scrollPane.getHorizontalScrollBar().isShowing();
            frame.dispose();
            if (failed) {
                throw new RuntimeException("The test failed");
            }
        }
    });
    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("The test is suitable only for Windows OS. Skipped");
            return;
        }
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JTextArea textArea = new JTextArea();
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                scrollPane = new JScrollPane(textArea);
                scrollPane.setMinimumSize(new Dimension(200, 100));
                scrollPane.setPreferredSize(new Dimension(300, 150));
                frame = new JFrame("Vertical scrollbar shown without text");
                frame.setContentPane(scrollPane);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
}

public class InvalidateMustRespectValidateRoots {
    private static volatile JRootPane rootPane;
    public static void main(String args[]) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                final JButton button = new JButton();
                frame.add(button);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        if (button.isValid()) {
                            button.invalidate();
                        } else {
                            button.revalidate();
                        }
                    }
                });
                rootPane = frame.getRootPane();
                frame.pack(); 
                frame.setVisible(true);
                if (!frame.isValid()) {
                    throw new RuntimeException(
                            "setVisible(true) failed to validate the frame");
                }
                button.invalidate();
                if (rootPane.isValid()) {
                    throw new RuntimeException(
                            "invalidate() failed to invalidate the root pane");
                }
                if (!frame.isValid()) {
                    throw new RuntimeException(
                            "invalidate() invalidated the frame");
                }
                button.revalidate();
            }
        });
        Thread.sleep(1000);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                if (!rootPane.isValid()) {
                    throw new RuntimeException(
                            "revalidate() failed to validate the hierarchy");
                }
            }
        });
    }
}

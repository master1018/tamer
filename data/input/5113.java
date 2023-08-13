public class bug6694823 {
    private static JFrame frame;
    private static JPopupMenu popup;
    private static SunToolkit toolkit;
    private static Insets screenInsets;
    public static void main(String[] args) throws Exception {
        toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                createGui();
            }
        });
        screenInsets = toolkit.getScreenInsets(frame.getGraphicsConfiguration());
        if (screenInsets.bottom == 0) {
            return;
        }
        showPopup(false);
        toolkit.realSync();
        System.setSecurityManager(new SecurityManager());
        showPopup(true);
        toolkit.realSync();
        System.out.println("Test passed!");
        frame.dispose();
    }
    private static void createGui() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        popup = new JPopupMenu("Menu");
        for (int i = 0; i < 7; i++) {
            popup.add(new JMenuItem("MenuItem"));
        }
        JPanel panel = new JPanel();
        panel.setComponentPopupMenu(popup);
        frame.add(panel);
        frame.setSize(200, 200);
    }
    private static void showPopup(final boolean shouldBeShifted) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension screenSize = toolkit.getScreenSize();
                frame.setLocation(screenSize.width / 2,
                        screenSize.height - frame.getHeight() - screenInsets.bottom);
                frame.setVisible(true);
                Point frameLoc = frame.getLocationOnScreen();
                int x = 0;
                int y = frame.getHeight()
                        - popup.getPreferredSize().height + screenInsets.bottom;
                popup.show(frame, x, y);
                if (shouldBeShifted) {
                    if (popup.getLocationOnScreen()
                            .equals(new Point(frameLoc.x, frameLoc.y + y))) {
                        throw new RuntimeException("Popup is not shifted");
                    }
                } else {
                    if (!popup.getLocationOnScreen()
                            .equals(new Point(frameLoc.x, frameLoc.y + y))) {
                        throw new RuntimeException("Popup is unexpectedly shifted");
                    }
                }
                popup.setVisible(false);
            }
        });
    }
}

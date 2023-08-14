public class bug6691503 {
    private JPopupMenu popupMenu;
    private JFrame frame;
    private boolean isAlwaysOnTop1 = false;
    private boolean isAlwaysOnTop2 = true;
    public static void main(String[] args) {
        bug6691503 test = new bug6691503();
        test.setupUI();
        test.testApplication();
        test.testApplet();
        test.checkResult();
        test.stopEDT();
    }
    private void setupUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame = new JFrame();
                frame.setVisible(true);
                popupMenu = new JPopupMenu();
                JMenuItem click = new JMenuItem("Click");
                popupMenu.add(click);
            }
        });
    }
    private void testApplication() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                popupMenu.show(frame, 0, 0);
                Window popupWindow = (Window)
                        (popupMenu.getParent().getParent().getParent().getParent());
                isAlwaysOnTop1 = popupWindow.isAlwaysOnTop();
                System.out.println(
                        "Application: popupWindow.isAlwaysOnTop() = " + isAlwaysOnTop1);
                popupMenu.setVisible(false);
            }
        });
    }
    private void testApplet() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.setSecurityManager(new SecurityManager());
                popupMenu.show(frame, 0, 0);
                Window popupWindow = (Window)
                        (popupMenu.getParent().getParent().getParent().getParent());
                isAlwaysOnTop2 = popupWindow.isAlwaysOnTop();
                System.out.println(
                        "Applet: popupWindow.isAlwaysOnTop() = " + isAlwaysOnTop2);
                popupMenu.setVisible(false);
            }
        });
    }
    private void checkResult() {
        ((SunToolkit)(Toolkit.getDefaultToolkit())).realSync();
        if (!isAlwaysOnTop1 || isAlwaysOnTop2) {
            throw new RuntimeException("Malicious applet can show always-on-top " +
                    "popup menu which has whole screen size");
        }
        System.out.println("Test passed");
    }
    private void stopEDT() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.dispose();
            }
        });
    }
}

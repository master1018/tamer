public class Test6541987 implements Runnable {
    private static final SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
    private static Robot robot;
    public static void main(String[] args) throws AWTException {
        robot = new Robot();
        start();
        click(KeyEvent.VK_ESCAPE);
        toolkit.realSync();
        start();
        click(KeyEvent.VK_1);
        click(KeyEvent.VK_0);
        click(KeyEvent.VK_ESCAPE);
        click(KeyEvent.VK_ESCAPE);
        toolkit.realSync();
        for (Window window : Window.getWindows()) {
            if (window.isVisible()) {
                throw new Error("found visible window: " + window.getName());
            }
        }
    }
    private static void start() {
        SwingUtilities.invokeLater(new Test6541987());
        click(KeyEvent.VK_ALT, KeyEvent.VK_H);
        click(KeyEvent.VK_TAB);
        click(KeyEvent.VK_TAB);
        click(KeyEvent.VK_TAB);
        click(KeyEvent.VK_TAB);
    }
    private static void click(int...keys) {
        toolkit.realSync();
        for (int key : keys) {
            robot.keyPress(key);
        }
        for (int key : keys) {
            robot.keyRelease(key);
        }
    }
    public void run() {
        String title = getClass().getName();
        JFrame frame = new JFrame(title);
        frame.setVisible(true);
        Color color = JColorChooser.showDialog(frame, title, Color.BLACK);
        if (color != null) {
            throw new Error("unexpected color: " + color);
        }
        frame.setVisible(false);
        frame.dispose();
    }
}

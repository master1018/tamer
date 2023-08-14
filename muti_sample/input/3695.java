public class Test6325652 {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    public static void main(String[] args) throws Throwable {
        SwingTest.start(Test6325652.class);
    }
    private static Robot robot;
    private JInternalFrame internal;
    public Test6325652(JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.add(create(0));
        desktop.add(this.internal = create(1));
        frame.add(desktop);
    }
    public void select() throws PropertyVetoException {
        this.internal.setSelected(true);
    }
    public static void stepFirst() throws AWTException {
        robot = new Robot(); 
        click(KeyEvent.VK_CONTROL, KeyEvent.VK_F9); 
    }
    public void stepFirstValidate() {
        if (!this.internal.isIcon()) {
            throw new Error("frame should be an icon");
        }
    }
    public static void stepSecond() {
        click(KeyEvent.VK_CONTROL, KeyEvent.VK_F6); 
        click(KeyEvent.VK_CONTROL, KeyEvent.VK_F5); 
    }
    public void stepSecondValidate() {
        if (this.internal.isIcon()) {
            throw new Error("frame should not be an icon");
        }
    }
    private static void click(int... keys) {
        for (int key : keys) {
            robot.keyPress(key);
        }
        for (int key : keys) {
            robot.keyRelease(key);
        }
    }
    private static JInternalFrame create(int index) {
        String text = "test" + index; 
        index = index * 3 + 1;
        JInternalFrame internal = new JInternalFrame(text, true, true, true, true);
        internal.getContentPane().add(new JTextArea(text));
        internal.setBounds(10 * index, 10 * index, WIDTH, HEIGHT);
        internal.setVisible(true);
        return internal;
    }
}

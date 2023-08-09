public class bug6495920 implements Thread.UncaughtExceptionHandler {
    public static void main(String[] args) throws Throwable {
        SwingTest.start(bug6495920.class);
    }
    private static Robot robot;
    private final JPanel panel;
    public bug6495920(JFrame frame) {
        JPopupMenu menu = new JPopupMenu() {
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                throw new AssertionError(visible ? "show popup" : "hide popup");
            }
        };
        for (int i = 0; i < 10; i++) {
            menu.add(new JMenuItem(String.valueOf(i)));
        }
        this.panel = new JPanel();
        this.panel.setComponentPopupMenu(menu);
        frame.add(this.panel);
    }
    public void firstShowPopup() throws Exception {
        Point point = this.panel.getLocation();
        SwingUtilities.convertPointToScreen(point, this.panel);
        robot = new Robot(); 
        robot.mouseMove(point.x + 1, point.y + 1);
        robot.mousePress(InputEvent.BUTTON3_MASK);
        Thread.currentThread().setUncaughtExceptionHandler(this);
        robot.mouseRelease(InputEvent.BUTTON3_MASK); 
    }
    public void secondHidePopup() {
        Point point = this.panel.getLocation();
        SwingUtilities.convertPointToScreen(point, this.panel);
        robot.mouseMove(point.x - 1, point.y - 1);
        Thread.currentThread().setUncaughtExceptionHandler(this);
        robot.mousePress(InputEvent.BUTTON1_MASK); 
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
    public void thirdValidate() throws Exception {
        Field key = BasicPopupMenuUI.class.getDeclaredField("MOUSE_GRABBER_KEY");
        key.setAccessible(true);
        Object grabber = AppContext.getAppContext().get(key.get(null));
        if (grabber == null) {
            throw new Exception("cannot find a mouse grabber in app's context");
        }
        Field field = grabber.getClass().getDeclaredField("grabbedWindow");
        field.setAccessible(true);
        Object window = field.get(grabber);
        if (window != null) {
            throw new Exception("interaction with GNOME is crippled");
        }
    }
    public void uncaughtException(Thread thread, Throwable throwable) {
        System.out.println(throwable);
    }
}

public class RestoreFocusOnDisabledComponentTest extends Applet {
    Frame frame = new Frame("Frame") {public String toString() {return "FRAME";}};
    Button b0 = new Button("button0") {public String toString() {return "B-0";}};
    Button b1 = new Button("button1") {public String toString() {return "B-1";}};
    volatile int nFocused;
    Robot robot;
    public static void main(String[] args) {
        RestoreFocusOnDisabledComponentTest app = new RestoreFocusOnDisabledComponentTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
    }
    public void start() {
        frame.add(b0);
        frame.add(b1);
        frame.setLayout(new FlowLayout());
        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(robot);
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager() {
            public boolean dispatchEvent(AWTEvent e) {
                if (e.getID() == FocusEvent.FOCUS_GAINED) {
                    if (e.getSource() == b1) {
                        b1.setEnabled(false);
                    } else if (e.getSource() == b0) {
                        if (++nFocused > 10) {
                            nFocused = -1;
                            throw new TestFailedException("Focus went into busy loop!");
                        }
                    }
                }
                return super.dispatchEvent(e);
            }
        });
        b0.setEnabled(false);
        Util.waitForIdle(robot);
        if (nFocused != -1) {
            System.out.println("Test passed.");
        }
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}

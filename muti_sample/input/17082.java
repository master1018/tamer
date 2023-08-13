public class DeiconifiedFrameLoosesFocus extends Applet {
    Robot robot;
    static final Frame frame = new Frame("Frame");
    public static void main(String[] args) {
        DeiconifiedFrameLoosesFocus app = new DeiconifiedFrameLoosesFocus();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        this.setLayout (new BorderLayout ());
    }
    public void start() {
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.ICONIFIED) ||
            !Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.NORMAL))
        {
            System.out.println("Frame.ICONIFIED or Frame.NORMAL state is unsupported.");
            return;
        }
        frame.setSize(100, 100);
        frame.setVisible(true);
        Util.waitForIdle(robot);
        if (!frame.isFocused()) {
            Util.clickOnTitle(frame, robot);
            Util.waitForIdle(robot);
        }
        if (!frame.isFocused()) {
            throw new Error("Test error: couldn't focus the Frame.");
        }
        test();
        System.out.println("Test passed.");
    }
    void test() {
        frame.setExtendedState(Frame.ICONIFIED);
        Util.waitForIdle(robot);
        frame.setExtendedState(Frame.NORMAL);
        Util.waitForIdle(robot);
        if (!frame.isFocused()) {
            throw new TestFailedException("the Frame didn't regain focus after restoring!");
        }
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}

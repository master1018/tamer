public class RequestFocusToDisabledCompTest extends Applet {
    Robot robot;
    JFrame frame = new JFrame("Frame");
    JButton b0 = new JButton("b0");
    JButton b1 = new JButton("b1");
    public static void main(String[] args) {
        RequestFocusToDisabledCompTest app = new RequestFocusToDisabledCompTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        frame.add(b0);
        frame.add(b1);
        frame.setLayout(new FlowLayout());
        frame.pack();
        b1.setEnabled(false);
    }
    public void start() {
        Util.showWindowWait(frame);
        if (!b0.hasFocus()) {
            if (!Util.focusComponent(b0, 2000)) {
                throw new TestErrorException("couldn't focus " + b0);
            }
        }
        if (!Util.focusComponent(b1, 2000)) {
            throw new TestFailedException("focus wasn't requested on disabled " + b1);
        }
        System.out.println("Test passed.");
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class TestErrorException extends RuntimeException {
    TestErrorException(String msg) {
        super("Unexpected error: " + msg);
    }
}

public class NoAutotransferToDisabledCompTest extends Applet {
    Robot robot;
    JFrame frame = new JFrame("Frame");
    JButton b0 = new JButton("b0");
    JButton b1 = new JButton("b1");
    JButton b2 = new JButton("b2");
    public static void main(String[] args) {
        NoAutotransferToDisabledCompTest app = new NoAutotransferToDisabledCompTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        frame.add(b0);
        frame.add(b1);
        frame.add(b2);
        frame.setLayout(new FlowLayout());
        frame.pack();
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                b1.setEnabled(false);
                b2.setEnabled(false);
            }
        });
    }
    public void start() {
        Util.showWindowWait(frame);
        if (!Util.focusComponent(b1, 2000)) {
            throw new TestErrorException("couldn't focus " + b1);
        }
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_SPACE);
        Util.waitForIdle(robot);
        if (!b0.hasFocus()) {
            throw new TestFailedException("focus wasn't auto-transfered properly!");
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

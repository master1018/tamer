public class IconifiedFrameFocusChangeTest extends Applet {
    Frame testFrame = new Frame("Test Frame");
    Frame otherFrame = new Frame("Other Frame");
    Button testButton = new Button("test button");
    Button otherButton = new Button("other button");
    Robot robot;
    public static void main(String[] args) {
        IconifiedFrameFocusChangeTest app = new IconifiedFrameFocusChangeTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        testFrame.add(testButton);
        testFrame.pack();
        otherFrame.add(otherButton);
        otherFrame.pack();
        otherFrame.setLocation(200, 0);
        testButton.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                testButton.requestFocus();
            }
        });
    }
    public void start() {
        otherFrame.setVisible(true);
        Util.waitForIdle(robot);
        testFrame.setVisible(true);
        Util.waitForIdle(robot);
        robot.delay(1000); 
        if (!testButton.hasFocus()) {
            testButton.requestFocus();
            Util.waitForIdle(robot);
            if (!testButton.hasFocus()) {
                throw new TestErrorException("couldn't focus " + testButton);
            }
        }
        Runnable action = new Runnable() {
            public void run() {
                testFrame.setExtendedState(Frame.ICONIFIED);
            }
        };
        if (!Util.trackFocusGained(otherButton, action, 2000, true)) {
            throw new TestFailedException("iconifying focused window didn't trigger focus change");
        }
        action = new Runnable() {
            public void run() {
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.delay(50);
                robot.keyRelease(KeyEvent.VK_SPACE);
            }
        };
        if (!Util.trackActionPerformed(otherButton, action, 2000, true)) {
            throw new TestFailedException("Java focus owner doesn't match to the native one");
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

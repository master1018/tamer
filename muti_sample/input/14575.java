public class FocusOwnerFrameOnClick extends Applet {
    Robot robot;
    Frame frame = new Frame("Frame");
    Window window = new Window(frame);
    Button fButton = new Button("fButton");
    Button wButton = new Button("wButton");
    AtomicBoolean focused = new AtomicBoolean(false);
    public static void main(String[] args) {
        FocusOwnerFrameOnClick app = new FocusOwnerFrameOnClick();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        frame.setLayout(new FlowLayout());
        frame.setSize(200, 200);
        frame.add(fButton);
        window.setLocation(300, 0);
        window.add(wButton);
        window.pack();
    }
    public void start() {
        frame.setVisible(true);
        Util.waitForIdle(robot);
        window.setVisible(true);
        Util.waitForIdle(robot);
        if (!wButton.hasFocus()) {
            if (!Util.trackFocusGained(wButton, new Runnable() {
                    public void run() {
                        Util.clickOnComp(wButton, robot);
                    }
                }, 2000, false))
            {
                throw new TestErrorException("wButton didn't gain focus on showing");
            }
        }
        Runnable clickAction = new Runnable() {
                public void run() {
                    Point loc = fButton.getLocationOnScreen();
                    Dimension dim = fButton.getSize();
                    robot.mouseMove(loc.x, loc.y + dim.height + 20);
                    robot.delay(50);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.delay(50);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                }
            };
        if (!Util.trackWindowGainedFocus(frame, clickAction, 2000, true)) {
            throw new TestFailedException("The frame wasn't focused on click");
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

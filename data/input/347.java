public class FrameJumpingToMouse extends Applet
{
    JFrame frame = new JFrame("Test jumping frame");
    Robot robot = Util.createRobot();
    public static void main(String[] args) {
        FrameJumpingToMouse test = new FrameJumpingToMouse();
        test.init();
        test.start();
    }
    public void init() {
        frame.setFocusableWindowState(false);
        frame.setBounds(100, 100, 100, 100);
    }
    public void start() {
        frame.setVisible(true);
        Util.waitTillShown(frame);
        Point loc = frame.getLocationOnScreen();
        robot.mouseMove(loc.x + frame.getWidth() / 4, loc.y + frame.getInsets().top / 2);
        robot.delay(50);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseMove(loc.x + 100, loc.y + 50);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(robot);
        loc = frame.getLocation();
        robot.mouseMove(loc.x + frame.getWidth() / 2, loc.y + frame.getHeight() / 2);
        Util.waitForIdle(robot);
        if (!(frame.getLocation().equals(loc))) {
            throw new RuntimeException("Test failed: frame is moving to mouse with grab!");
        }
        System.out.println("Test passed.");
    }
}

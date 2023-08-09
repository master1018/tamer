public class OwnedWindowFocusIMECrashTest {
    Robot robot;
    JFrame owner = new JFrame("Owner Frame");
    JFrame frame = new JFrame("Other Frame");
    JWindow window = new JWindow(owner);
    JButton button = new JButton("Button");
    public static void main(String[] args) {
        OwnedWindowFocusIMECrashTest app = new OwnedWindowFocusIMECrashTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
    }
    public void start() {
        owner.setBounds(100, 100, 200, 100);
        window.setBounds(100, 250, 200, 100);
        frame.setBounds(350, 100, 200, 100);
        window.add(button);
        owner.setVisible(true);
        frame.setVisible(true);
        window.setVisible(true);
        Util.waitForIdle(robot);
        test();
        System.out.println("Test passed");
    }
    void test() {
        Util.clickOnComp(button, robot);
        if (!button.hasFocus()) {
            throw new TestErrorException("the button couldn't be focused by click");
        }
        Util.clickOnTitle(frame, robot); 
    }
}
class TestErrorException extends RuntimeException {
    TestErrorException(String msg) {
        super("Unexpected error: " + msg);
    }
}

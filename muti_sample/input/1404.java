public class CloseDialogActivateOwnerTest extends Applet {
    Robot robot;
    public static void main(String[] args) {
        CloseDialogActivateOwnerTest app = new CloseDialogActivateOwnerTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
    }
    public void start() {
        final Frame frame = new Frame("Owner Frame");
        final Dialog dialog = new Dialog(frame, "Owned Dialog");
        frame.setSize(100, 100);
        dialog.setSize(100, 100);
        if (!Util.trackWindowGainedFocus(frame, new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            }, 2000, false))
        {
            throw new TestErrorException("the owner frame hasn't been activated on show");
        }
        if (!Util.trackWindowGainedFocus(dialog, new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            }, 2000, true))
        {
            throw new TestErrorException("the owned dialog hasn't been activated on show");
        }
        robot.delay(2000); 
        if (!Util.trackWindowGainedFocus(frame, new Runnable() {
                public void run() {
                    dialog.dispose();
                }
            }, 2000, false))
        {
            throw new TestFailedException("the owner hasn't been activated on closing the owned dialog");
        }
        System.out.println("Test passed.");
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class TestErrorException extends Error {
    TestErrorException(String msg) {
        super("Unexpected error: " + msg);
    }
}

public class AppletInitialFocusTest extends Applet {
    Robot robot = Util.createRobot();
    Button button = new Button("Button");
    public void init() {
        add(button);
    }
    public void start() {
        new Thread(new Runnable() {
                public void run() {
                    Util.waitTillShown(button);
                    robot.delay(1000); 
                    Util.waitForIdle(robot);
                    if (!button.hasFocus()) {
                        throw new RuntimeException("Appletviewer doesn't set default focus correctly.");
                    }
                }
            }).start();
    }
}

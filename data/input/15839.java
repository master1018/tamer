public class ConsumeNextKeyTypedOnModalShowTest extends Applet {
    Robot robot;
    Frame frame = new Frame("Frame");
    Dialog dialog = new Dialog(frame, "Dialog", true);
    TextField tf0 = new TextField();
    TextField tf1 = new TextField();
    Button button = new Button("Button");
    public static void main(String[] args) {
        ConsumeNextKeyTypedOnModalShowTest app = new ConsumeNextKeyTypedOnModalShowTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        tf0.setPreferredSize(new Dimension(50, 30));
        tf1.setPreferredSize(new Dimension(50, 30));
        frame.setLayout(new FlowLayout());
        frame.add(tf0);
        frame.add(tf1);
        frame.pack();
        dialog.add(button);
        dialog.pack();
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getSource() == tf0) {
                    dialog.setVisible(true);
                }
            }
        }, KeyEvent.KEY_EVENT_MASK);
    }
    public void start() {
        frame.setVisible(true);
        Util.waitTillShown(frame);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_TAB);
        Util.waitForIdle(robot);
        Runnable action = new Runnable() {
            public void run() {
                dialog.dispose();
            }
        };
        if (!Util.trackFocusGained(tf1, action, 2000, false)) {
            throw new RuntimeException("Test failed: TAB was processed incorrectly!");
        }
        robot.keyPress(KeyEvent.VK_A);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_A);
        Util.waitForIdle(robot);
        if (tf1.getText().equals("")) {
            throw new RuntimeException("Test failed: couldn't type a char!");
        }
        System.out.println("Test passed.");
    }
}

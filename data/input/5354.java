public class TestFocusFreeze {
    private static JFrame frame;
    private static JDialog dialog;
    private static JButton dlgButton;
    private static JButton frameButton;
    private static AtomicBoolean lock = new AtomicBoolean(false);
    private static Robot robot = Util.createRobot();
    public static void main(String[] args) {
        boolean all_passed = true;
        KeyboardFocusManager testKFM = new TestKFM(robot);
        KeyboardFocusManager defKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        for (int i = 0; i < 10; i++) {
            test(testKFM, defKFM);
            Util.waitForIdle(robot);
            System.out.println("Iter " + i + ": " + (lock.get() ? "passed." : "failed!"));
            if (!lock.get()) {
                all_passed = false;
            }
        }
        if (!all_passed) {
            throw new RuntimeException("Test failed: not all iterations passed!");
        }
        System.out.println("Test passed.");
    }
    public static void test(final KeyboardFocusManager testKFM, final KeyboardFocusManager defKFM) {
        frame = new JFrame("Frame");
        dialog = new JDialog(frame, "Dialog", true);
        dlgButton = new JButton("Dialog_Button");
        frameButton = new JButton("Frame_Button");
        lock.set(false);
        dialog.add(dlgButton);
        dialog.setLocation(200, 0);
        dialog.pack();
        frame.add(frameButton);
        frame.pack();
        dlgButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                frame.dispose();
                synchronized (lock) {
                    lock.set(true);
                    lock.notifyAll();
                }
            }
        });
        frameButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    KeyboardFocusManager.setCurrentKeyboardFocusManager(testKFM);
                    dialog.setVisible(true);
                    KeyboardFocusManager.setCurrentKeyboardFocusManager(defKFM);
                }
            });
        Runnable showAction = new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        };
        if (!Util.trackFocusGained(frameButton, showAction, 2000, false)) {
            System.out.println("Test error: wrong initial focus!");
            return;
        }
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_SPACE);
        Util.waitForCondition(lock, 2000);
        Util.waitForIdle(robot);
    }
}
class TestKFM extends DefaultKeyboardFocusManager {
    Robot robot;
    public TestKFM(Robot robot) {
        this.robot = robot;
    }
    protected synchronized void enqueueKeyEvents(long after, Component untilFocused) {
        super.enqueueKeyEvents(after, untilFocused);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_SPACE);
    }
}

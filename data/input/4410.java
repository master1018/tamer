public class WrongKeyTypedConsumedTest extends Applet
{
    Robot robot = Util.createRobot();
    public static void main(String[] args) {
        WrongKeyTypedConsumedTest test = new WrongKeyTypedConsumedTest();
        test.start();
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        JFrame frame = new JFrame("The Frame");
        Set ftk = new HashSet();
        ftk.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0));
        frame.getContentPane().
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                  ftk);
        JCheckBox checkbox = new JCheckBox("test");
        frame.getContentPane().add(checkbox, BorderLayout.NORTH);
        JTextArea textarea = new JTextArea(40, 10);
        frame.getContentPane().add(textarea);
        frame.pack();
        frame.setVisible(true);
        Util.waitForIdle(robot);
        if (!frame.isActive()) {
            throw new RuntimeException("Test Fialed: frame isn't active");
        }
        if (!checkbox.isFocusOwner()) {
            checkbox.requestFocusInWindow();
            Util.waitForIdle(robot);
            if (!checkbox.isFocusOwner()) {
                throw new RuntimeException("Test Failed: checkbox doesn't have focus");
            }
        }
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_DOWN);
        robot.delay(50);
        Util.waitForIdle(robot);
        if (!textarea.isFocusOwner()) {
            throw new RuntimeException("Test Failed: focus wasn't transfered to text area");
        }
        robot.keyPress(KeyEvent.VK_1);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_1);
        robot.delay(50);
        Util.waitForIdle(robot);
        if (!"1".equals(textarea.getText())) {
            throw new RuntimeException("Test Failed: text area text is \"" + textarea.getText() + "\", not \"1\"");
        }
        System.out.println("Test Passed");
    }
}

public class NonfocusableOwnerTest extends Applet {
    Robot robot = Util.createRobot();
    Frame frame;
    Dialog dialog;
    Window window1;
    Window window2;
    Button button = new Button("button");
    public static void main(String[] args) {
        NonfocusableOwnerTest test = new NonfocusableOwnerTest();
        test.start();
    }
    public void start() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                public void eventDispatched(AWTEvent e) {
                    System.out.println(e.toString());
                }
            }, FocusEvent.FOCUS_EVENT_MASK | WindowEvent.WINDOW_FOCUS_EVENT_MASK | WindowEvent.WINDOW_EVENT_MASK);
        frame = new Frame("Frame");
        frame.setName("Frame-owner");
        frame.setBounds(100, 0, 100, 100);
        dialog = new Dialog(frame, "Dialog");
        dialog.setName("Dialog-owner");
        dialog.setBounds(100, 0, 100, 100);
        window1 = new Window(frame);
        window1.setName("1st child");
        window1.setBounds(100, 300, 100, 100);
        window2 = new Window(window1);
        window2.setName("2nd child");
        window2.setBounds(100, 500, 100, 100);
        test1(frame, window1);
        test2(frame, window1, window2);
        test3(frame, window1, window2);
        window1 = new Window(dialog);
        window1.setBounds(100, 300, 100, 100);
        window1.setName("1st child");
        window2 = new Window(window1);
        window2.setName("2nd child");
        window2.setBounds(100, 500, 100, 100);
        test1(dialog, window1);
        test2(dialog, window1, window2);
        test3(dialog, window1, window2);
        System.out.println("Test passed.");
    }
    void test1(Window owner, Window child) {
        System.out.println("* * * STAGE 1 * * *\nWindow owner: " + owner);
        owner.setFocusableWindowState(false);
        owner.setVisible(true);
        child.add(button);
        child.setVisible(true);
        Util.waitTillShown(child);
        Util.clickOnComp(button, robot);
        if (button == KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) {
            throw new RuntimeException("Test Failed.");
        }
        child.dispose();
        owner.dispose();
    }
    void test2(Window owner, Window child1, Window child2) {
        System.out.println("* * * STAGE 2 * * *\nWindow nowner: " + owner);
        owner.setFocusableWindowState(false);
        owner.setVisible(true);
        child1.setFocusableWindowState(true);
        child1.setVisible(true);
        child2.add(button);
        child2.setVisible(true);
        Util.waitTillShown(child2);
        Util.clickOnComp(button, robot);
        if (button == KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) {
            throw new RuntimeException("Test failed.");
        }
        child2.dispose();
        child1.dispose();
        owner.dispose();
    }
    void test3(Window owner, Window child1, Window child2) {
        System.out.println("* * * STAGE 3 * * *\nWidow owner: " + owner);
        owner.setFocusableWindowState(true);
        owner.setVisible(true);
        child1.setFocusableWindowState(false);
        child1.setVisible(true);
        child2.setFocusableWindowState(true);
        child2.add(button);
        child2.setVisible(true);
        Util.waitTillShown(child2);
        Util.clickOnComp(button, robot);
        System.err.println("focus owner: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
        if (button != KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) {
            throw new RuntimeException("Test failed.");
        }
        child1.dispose();
        child2.dispose();
        owner.dispose();
    }
}

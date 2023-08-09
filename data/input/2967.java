public class ActualFocusedWindowBlockingTest extends Applet {
    Robot robot = Util.createRobot();
    Frame owner = new Frame("Owner Frame");
    Window win = new Window(owner);
    Frame frame = new Frame("Auxiliary Frame");
    Button fButton = new Button("frame button") {public String toString() {return "Frame_Button";}};
    Button wButton = new Button("window button") {public String toString() {return "Window_Button";}};
    Button aButton = new Button("auxiliary button") {public String toString() {return "Auxiliary_Button";}};
    public static void main(String[] args) {
        ActualFocusedWindowBlockingTest app = new ActualFocusedWindowBlockingTest();
        app.init();
        app.start();
    }
    public void init() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                public void eventDispatched(AWTEvent e) {
                    System.out.println("--> " + e);
                }
            }, FocusEvent.FOCUS_EVENT_MASK | WindowEvent.WINDOW_FOCUS_EVENT_MASK);
        owner.add(fButton);
        win.add(wButton);
        frame.add(aButton);
        owner.setName("OWNER_FRAME");
        win.setName("OWNED_WINDOW");
        frame.setName("AUX_FRAME");
        tuneAndShowWindows(new Window[] {owner, win, frame});
    }
    public void start() {
        if ("sun.awt.motif.MToolkit".equals(Toolkit.getDefaultToolkit().getClass().getName())) {
            System.out.println("No testing on Motif. Test passed.");
            return;
        }
        System.out.println("\nTest started:\n");
        clickOnCheckFocus(wButton);
        clickOnCheckFocus(aButton);
        Util.clickOnComp(fButton, robot);
        if (!testFocused(fButton)) {
            throw new TestFailedException("The owner's component [" + fButton + "] couldn't be focused by click");
        }
        clickOnCheckFocus(wButton);
        clickOnCheckFocus(aButton);
        fButton.requestFocus();
        Util.waitForIdle(robot);
        if (!testFocused(fButton)) {
            throw new TestFailedException("The owner's component [" + fButton + "] couldn't be focused by request");
        }
        clickOnCheckFocus(wButton);
        clickOnCheckFocus(aButton);
        clickOnCheckFocus(fButton);
        clickOnCheckFocus(aButton);
        Util.clickOnTitle(owner, robot);
        if (!testFocused(fButton)) {
            throw new TestFailedException("The owner's component [" + fButton + "] couldn't be focused as the most recent focus owner");
        }
        System.out.println("Test passed.");
    }
    void tuneAndShowWindows(Window[] arr) {
        int y = 0;
        for (Window w: arr) {
            w.setLayout(new FlowLayout());
            w.setBounds(100, y, 400, 150);
            w.setBackground(Color.blue);
            w.setVisible(true);
            y += 200;
            Util.waitForIdle(robot);
        }
    }
    void clickOnCheckFocus(Component c) {
        if (c instanceof Frame) {
            Util.clickOnTitle((Frame)c, robot);
        } else {
            Util.clickOnComp(c, robot);
        }
        if (!testFocused(c)) {
            throw new TestErrorException(c + "couldn't get focus by click.");
        }
    }
    boolean testFocused(Component c) {
        for (int i=0; i<10; i++) {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == c) {
                return true;
            }
            Util.waitForIdle(robot);
        }
        return false;
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
}

public class AutoRequestFocusSetVisibleTest extends Applet {
    static Frame focusedFrame;
    static Button focusOwner;
    static Frame frame;
    static Button frameButton;
    static Frame frame2;
    static Button frameButton2;
    static Window window;
    static Button winButton;
    static Window ownedWindow;
    static Button ownWinButton;
    static Dialog ownedDialog;
    static Button ownDlgButton;
    static Dialog dialog;
    static Button dlgButton;
    static String toolkitClassName;
    static Robot robot = Util.createRobot();
    public static void main(String[] args) {
        AutoRequestFocusSetVisibleTest app = new AutoRequestFocusSetVisibleTest();
        app.init();
        app.start();
    }
    public void init() {
        this.setLayout (new BorderLayout ());
        Sysout.createDialogWithInstructions(new String[]
            {"This is an automatic test. Simply wait until it is done."
            });
        toolkitClassName = Toolkit.getDefaultToolkit().getClass().getName();
    }
    void recreateGUI() {
        if (focusedFrame != null) {
            focusedFrame.dispose();
            frame.dispose();
            frame2.dispose();
            window.dispose();
            ownedWindow.dispose();
            ownedDialog.dispose();
            dialog.dispose();
        }
        focusedFrame = new Frame("Base Frame");
        focusOwner = new Button("button");
        frame = new Frame("Test Frame");
        frameButton = new Button("button");
        frame2 = new Frame("Test Frame");
        frameButton2 = new Button("button");
        window = new Window(focusedFrame);
        winButton = new Button("button");
        ownedWindow = new Window(frame) {
                public void show() {
                    robot.delay(100);
                    super.show();
                }
            };
        ownWinButton = new Button("button");
        ownedDialog = new Dialog(frame2);
        ownDlgButton = new Button("button");
        dialog = new Dialog(focusedFrame, "Test Dialog");
        dlgButton = new Button("button");
        focusedFrame.add(focusOwner);
        focusedFrame.setBounds(100, 100, 300, 300);
        frame.setBounds(140, 140, 220, 220);
        frame.add(frameButton);
        frame2.setBounds(140, 140, 220, 220);
        frame2.add(frameButton2);
        window.setBounds(140, 140, 220, 220);
        window.add(winButton);
        ownedWindow.setBounds(180, 180, 140, 140);
        ownedWindow.add(ownWinButton);
        ownedDialog.setBounds(180, 180, 140, 140);
        ownedDialog.add(ownDlgButton);
        dialog.setBounds(140, 140, 220, 220);
        dialog.add(dlgButton);
    }
    public void start() {
        recreateGUI();
        Sysout.println("Stage 1 in progress...");
        dialog.setModal(true);
        dialog.setAutoRequestFocus(false);
        setVisible(focusedFrame, true);
        TestHelper.invokeLaterAndWait(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            }, robot);
        if (focusOwner.hasFocus()) {
            throw new TestFailedException("the modal dialog must gain focus but it didn't!");
        }
        setVisible(dialog, false);
        recreateGUI();
        Sysout.println("Stage 2 in progress...");
        setVisible(focusedFrame, false);
        focusedFrame.setAutoRequestFocus(false);
        setVisible(focusedFrame, true);
        Util.clickOnTitle(focusedFrame, robot);
        Util.waitForIdle(robot);
        if (!focusedFrame.isFocused()) {
            throw new Error("Test error: the frame couldn't be focused.");
        }
        focusedFrame.setExtendedState(Frame.ICONIFIED);
        Util.waitForIdle(robot);
        focusedFrame.setExtendedState(Frame.NORMAL);
        Util.waitForIdle(robot);
        if (!focusedFrame.isFocused()) {
            throw new TestFailedException("the restored frame must gain focus but it didn't!");
        }
        recreateGUI();
        test("Stage 3.1 in progress...", frame, frameButton);
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
            System.out.println("Stage 3.2: Frame.MAXIMIZED_BOTH not supported. Skipping.");
        } else {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            test("Stage 3.2 in progress...", frame, frameButton);
        }
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_VERT)) {
            System.out.println("Stage 3.3: Frame.MAXIMIZED_VERT not supported. Skipping.");
        } else {
            frame.setExtendedState(Frame.MAXIMIZED_VERT);
            test("Stage 3.3 in progress...", frame, frameButton);
        }
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_HORIZ)) {
            System.out.println("Stage 3.4: Frame.MAXIMIZED_HORIZ not supported. Skipping.");
        } else {
            frame.setExtendedState(Frame.MAXIMIZED_HORIZ);
            test("Stage 3.4 in progress...", frame, frameButton);
        }
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.ICONIFIED)) {
            System.out.println("Stage 3.5: Frame.ICONIFIED not supported. Skipping.");
        } else {
            frame.setExtendedState(Frame.ICONIFIED);
            test("Stage 3.5 in progress...", frame, frameButton);
        }
        recreateGUI();
        test("Stage 4.1 in progress...", window, winButton);
        test("Stage 4.2 in progress...", dialog, dlgButton);
        dialog.setModal(true);
        test("Stage 4.3 in progress...", dialog, dlgButton, true);
        if ("sun.awt.windows.WToolkit".equals(toolkitClassName)) {
            Sysout.println("Stage 5.1 - Skiping.");
        } else {
            setVisible(ownedWindow, true);
            setVisible(frame, false); 
            test("Stage 5.1 in progress...", frame, ownedWindow, ownWinButton, true);
        }
        setVisible(ownedDialog, true);
        setVisible(frame2, false); 
        test("Stage 5.2 in progress...", frame2, ownedDialog, ownDlgButton, true);
        if ("sun.awt.motif.MToolkit".equals(toolkitClassName)) {
            Sysout.println("Stage 6 - Skiping.");
        } else {
            Sysout.println("Stage 6 in progress...");
            Frame f = new Frame("Aux. Frame");
            f.setSize(100, 100);
            setVisible(f, true);
            setVisible(focusedFrame, true);
            if (!focusOwner.hasFocus()) {
                Util.clickOnComp(focusOwner, robot);
                Util.waitForIdle(robot);
                if (!focusOwner.hasFocus()) {
                    throw new Error("Test error: the frame couldn't be focused.");
                }
            }
            dialog.setModal(true);
            dialog.setAutoRequestFocus(false);
            focusedFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
            TestHelper.invokeLaterAndWait(new Runnable() {
                    public void run() {
                        dialog.setVisible(true);
                    }
                }, robot);
            if (dialog.isFocused()) {
                throw new TestFailedException("the unblocking dialog shouldn't gain focus but it did!");
            }
            setVisible(dialog, false);
        }
        Sysout.println("Test passed.");
    }
    void test(String msg, final Window showWindow, Window ownedWindow, final Button clickButton, boolean shouldFocusChange) {
        Window testWindow = (ownedWindow == null ? showWindow : ownedWindow);
        Sysout.println(msg);
        if (showWindow.isVisible()) {
            showWindow.dispose();
            Util.waitForIdle(robot);
        }
        if (!focusedFrame.isVisible()) {
            setVisible(focusedFrame, true);
        }
        if (!focusOwner.hasFocus()) {
            Util.clickOnComp(focusOwner, robot);
            Util.waitForIdle(robot);
            if (!focusOwner.hasFocus()) {
                throw new Error("Test error: the frame couldn't be focused.");
            }
        }
        final Runnable showAction = new Runnable() {
                public void run() {
                    showWindow.setAutoRequestFocus(false);
                    showWindow.setVisible(true);
                }
            };
        final Runnable trackerAction = new Runnable() {
                public void run() {
                    if (showWindow instanceof Dialog && ((Dialog)showWindow).isModal()) {
                        TestHelper.invokeLaterAndWait(showAction, robot);
                    } else {
                        showAction.run();
                    }
                }
            };
        if (shouldFocusChange) {
            trackerAction.run();
            Util.waitForIdle(robot);
            if (!testWindow.isFocused()) {
                throw new TestFailedException("the window must gain focus but it didn't!");
            }
        } else if (TestHelper.trackFocusChangeFor(trackerAction, robot)) {
            throw new TestFailedException("the window shouldn't gain focus but it did!");
        }
        if (!(testWindow instanceof Frame) ||
            ((Frame)testWindow).getExtendedState() != Frame.ICONIFIED)
        {
            boolean performed = Util.trackActionPerformed(clickButton, new Runnable() {
                    public void run() {
                        Util.clickOnComp(clickButton, robot);
                    }
                }, 1000, false);
            if (!performed) {
                Sysout.println("(ACTION_EVENT was not generated. One more attemp.)");
                performed = Util.trackActionPerformed(clickButton, new Runnable() {
                        public void run() {
                            Util.clickOnComp(clickButton, robot);
                        }
                    }, 1000, false);
                if (!performed) {
                    throw new TestFailedException("the window shown is not on the top!");
                }
            }
        }
        recreateGUI();
    }
    void test(String msg, final Window showWindow, Button clickButton) {
        test(msg, showWindow, null, clickButton, false);
    }
    void test(String msg, final Window showWindow, Button clickButton, boolean shouldFocusChange) {
        test(msg, showWindow, null, clickButton, shouldFocusChange);
    }
    void test(String msg, final Window showWindow, Window ownedWindow, Button clickButton) {
        test(msg, showWindow, ownedWindow, clickButton, false);
    }
    private static void setVisible(Window w, boolean b) {
        w.setVisible(b);
        try {
            Util.waitForIdle(robot);
        } catch (RuntimeException rte) { 
            rte.printStackTrace();
        }
        robot.delay(200);
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class Sysout
{
    static TestDialog dialog;
    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        println( "Any messages for the tester will display here." );
    }
    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }
    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }
}
class TestDialog extends Dialog
{
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );
        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);
        pack();
    }
    public void printInstructions( String[] instructions )
    {
        instructionsText.setText( "" );
        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ )
        {
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 )
            {
                if( remainingStr.length() >= maxStringLength )
                {
                    int posOfSpace = remainingStr.
                        lastIndexOf( ' ', maxStringLength - 1 );
                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;
                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                else
                {
                    printStr = remainingStr;
                    remainingStr = "";
                }
                instructionsText.append( printStr + "\n" );
            }
        }
    }
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }
}

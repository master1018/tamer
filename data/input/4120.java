public class ShowFrameCheckForegroundTest extends Applet {
    Robot robot;
    Frame nofocusFrame = new Frame("Non-focusable");
    Frame frame = new Frame("Frame");
    Dialog dialog1 = new Dialog(nofocusFrame, "Owned Dialog", false);
    Dialog dialog2 = new Dialog((Frame)null, "Owned Dialog", false);
    Window testToplevel = null;
    Button testButton = new Button("button");
    Button showButton = new Button("show");
    Runnable action = new Runnable() {
        public void run() {
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
        }
    };
    public static void main(String[] args) {
        ShowFrameCheckForegroundTest app = new ShowFrameCheckForegroundTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
    }
    public void start() {
        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testToplevel.setVisible(true);
            }
        });
        nofocusFrame.add(showButton);
        nofocusFrame.pack();
        nofocusFrame.setFocusableWindowState(false);
        nofocusFrame.setVisible(true);
        Util.waitForIdle(robot);
        robot.delay(3000);
        test(frame, 1);
        test(dialog1, 1);
        test(dialog2, 1);
        test(frame, 2);
        test(dialog1, 2);
        test(dialog2, 2);
        System.out.println("Test passed.");
    }
    private void test(Window toplevel, int stage) {
        toplevel.add(testButton);
        toplevel.pack();
        toplevel.setLocation(200, 0);
        switch (stage) {
            case 1:
                toplevel.setVisible(true);
                break;
            case 2:
                testToplevel = toplevel;
                Util.clickOnComp(showButton, robot);
                break;
        }
        Util.waitForIdle(robot);
        if (!Util.trackActionPerformed(testButton, action, 2000, false)) {
            throw new TestFailedException("Stage " + stage + ". The toplevel " + toplevel + " wasn't made foreground on showing");
        }
        System.out.println("Stage " + stage + ". Toplevel " + toplevel + " - passed");
        toplevel.dispose();
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class Sysout {
    static TestDialog dialog;
    public static void createDialogWithInstructions( String[] instructions ) {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( ) {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }
    public static void printInstructions( String[] instructions ) {
        dialog.printInstructions( instructions );
    }
    public static void println( String messageIn ) {
        dialog.displayMessage( messageIn );
    }
}
class TestDialog extends Dialog {
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
    public TestDialog( Frame frame, String name ) {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );
        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);
        pack();
        setVisible(true);
    }
    public void printInstructions( String[] instructions ) {
        instructionsText.setText( "" );
        String printStr, remainingStr;
        for( int i=0; i < instructions.length; i++ ) {
            remainingStr = instructions[ i ];
            while( remainingStr.length() > 0 ) {
                if( remainingStr.length() >= maxStringLength ) {
                    int posOfSpace = remainingStr.
                            lastIndexOf( ' ', maxStringLength - 1 );
                    if( posOfSpace <= 0 ) posOfSpace = maxStringLength - 1;
                    printStr = remainingStr.substring( 0, posOfSpace + 1 );
                    remainingStr = remainingStr.substring( posOfSpace + 1 );
                }
                else {
                    printStr = remainingStr;
                    remainingStr = "";
                }
                instructionsText.append( printStr + "\n" );
            }
        }
    }
    public void displayMessage( String messageIn ) {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }
}

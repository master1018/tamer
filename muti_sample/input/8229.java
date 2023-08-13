public class ModalBlockedStealsFocusTest extends Applet {
    SunToolkit toolkit = (SunToolkit)Toolkit.getDefaultToolkit();
    Frame frame = new Frame("Blocked Frame");
    Dialog dialog = new Dialog(frame, "Modal Dialog", Dialog.ModalityType.TOOLKIT_MODAL);
    AtomicBoolean lostFocus = new AtomicBoolean(false);
    public static void main(String[] args) {
        ModalBlockedStealsFocusTest app = new ModalBlockedStealsFocusTest();
        app.init();
        app.start();
    }
    public void init() {
        this.setLayout (new BorderLayout ());
        Sysout.createDialogWithInstructions(new String[]
            {"This is an automatic test. Simply wait until it is done."
            });
    }
    public void start() {
        if ("sun.awt.motif.MToolkit".equals(Toolkit.getDefaultToolkit().getClass().getName())) {
            Sysout.println("The test is not for MToolkit.");
            return;
        }
        dialog.setBounds(800, 0, 200, 100);
        frame.setBounds(800, 150, 200, 100);
        dialog.addWindowFocusListener(new WindowAdapter() {
                public void windowLostFocus(WindowEvent e) {
                    Sysout.println(e.toString());
                    synchronized (lostFocus) {
                        lostFocus.set(true);
                        lostFocus.notifyAll();
                    }
                }
            });
        new Thread(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            }).start();
        Util.waitTillShown(dialog);
        toolkit.realSync();
        frame.setVisible(true);
        if (Util.waitForCondition(lostFocus, 2000L)) {
            throw new TestFailedException("the modal blocked frame stole focus on its showing!");
        }
        frame.toFront();
        if (Util.waitForCondition(lostFocus, 2000L)) {
            throw new TestFailedException("the modal blocked frame stole focus on its bringing to front!");
        } else {
            Sysout.println("Test passed");
        }
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
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
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
        setVisible(true);
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

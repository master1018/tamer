public class WindowInitialFocusTest extends Applet {
    Frame frame = new Frame("Test Frame");
    Window window = new Window(frame);
    Button button = new Button("button");
    AtomicBoolean focused = new AtomicBoolean(false);
    SunToolkit toolkit = (SunToolkit)Toolkit.getDefaultToolkit();
    public static void main(String[] args) {
        WindowInitialFocusTest app = new WindowInitialFocusTest();
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
        frame.setBounds(800, 0, 200, 100);
        window.setBounds(800, 200, 200, 100);
        window.setLayout(new FlowLayout());
        window.add(button);
        window.addWindowFocusListener(new WindowAdapter() {
                public void windowGainedFocus(WindowEvent e) {
                    Sysout.println(e.toString());
                    synchronized (focused) {
                        focused.set(true);
                        focused.notifyAll();
                    }
                }});
        frame.setVisible(true);
        toolkit.realSync();
        window.setVisible(true);
        toolkit.realSync();
        if (!Util.waitForCondition(focused, 2000L)) {
            throw new TestFailedException("the window didn't get focused on its showing!");
        }
        window.setVisible(false);
        toolkit.realSync();
        window.setFocusableWindowState(false);
        focused.set(false);
        window.setVisible(true);
        toolkit.realSync();
        if (Util.waitForCondition(focused, 2000L)) {
            throw new TestFailedException("the unfocusable window got focused on its showing!");
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

public class MaximizedToIconified
{
    static volatile int lastFrameState = Frame.NORMAL;
    static volatile boolean failed = false;
    static volatile Toolkit myKit;
    private static void checkState(Frame f, int state) {
        f.setExtendedState(state);
        Util.waitForIdle(null);
        System.out.println("state = " + state + "; getExtendedState() = " + f.getExtendedState());
        if (failed) {
            MaximizedToIconified.fail("getOldState() != previous getNewState() in WINDOW_STATE_CHANGED event.");
        }
        if (lastFrameState != f.getExtendedState()) {
            MaximizedToIconified.fail("getExtendedState() != last getNewState() in WINDOW_STATE_CHANGED event.");
        }
        if (f.getExtendedState() != state) {
            MaximizedToIconified.fail("getExtendedState() != " + state + " as expected.");
        }
    }
    private static void examineStates(Frame f_arg, int states[]) {
        Frame f = f_arg;
        if (f == null) {
            f = new Frame("test");
            f.setSize(200, 200);
            f.setVisible(true);
        }
        Util.waitForIdle(null);
        f.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                System.out.println("last = " + lastFrameState + "; getOldState() = " + e.getOldState() + "; getNewState() = " + e.getNewState());
                if (e.getOldState() == lastFrameState) {
                    lastFrameState = e.getNewState();
                } else {
                    System.out.println("Wrong getOldState(): expected = " + lastFrameState + "; received = " + e.getOldState());
                    failed = true;
                }
            }
        });
        for (int state: states) {
            if (myKit.isFrameStateSupported(state)) {
                checkState(f, state);
            } else {
                System.out.println("Frame state = " + state + " is NOT supported by the native system. The state is skipped.");
            }
        }
        if (f_arg == null) {
            f.dispose();
        }
    }
    private static void init()
    {
        String[] instructions =
        {
            "This is an AUTOMATIC test, simply wait until it is done.",
            "The result (passed or failed) will be shown in the",
            "message window below."
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );
        myKit = Toolkit.getDefaultToolkit();
        examineStates(null, new int[] {Frame.MAXIMIZED_BOTH, Frame.ICONIFIED, Frame.NORMAL});
        examineStates(null, new int[] {Frame.ICONIFIED, Frame.MAXIMIZED_BOTH, Frame.NORMAL});
        MaximizedToIconified.pass();
    }
    private static boolean theTestPassed = false;
    private static boolean testGeneratedInterrupt = false;
    private static String failureMessage = "";
    private static Thread mainThread = null;
    private static int sleepTime = 300000;
    public static void main( String args[] ) throws InterruptedException
    {
        mainThread = Thread.currentThread();
        try
        {
            init();
        }
        catch( TestPassedException e )
        {
            return;
        }
        try
        {
            Thread.sleep( sleepTime );
            throw new RuntimeException( "Timed out after " + sleepTime/1000 + " seconds" );
        }
        catch (InterruptedException e)
        {
            if( ! testGeneratedInterrupt ) throw e;
            testGeneratedInterrupt = false;
            if ( theTestPassed == false )
            {
                throw new RuntimeException( failureMessage );
            }
        }
    }
    public static synchronized void setTimeoutTo( int seconds )
    {
        sleepTime = seconds * 1000;
    }
    public static synchronized void pass()
    {
        Sysout.println( "The test passed." );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        if ( mainThread == Thread.currentThread() )
        {
            theTestPassed = true;
            throw new TestPassedException();
        }
        theTestPassed = true;
        testGeneratedInterrupt = true;
        mainThread.interrupt();
    }
    public static synchronized void fail()
    {
        fail( "it just plain failed! :-)" );
    }
    public static synchronized void fail( String whyFailed )
    {
        Sysout.println( "The test failed: " + whyFailed );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        if ( mainThread == Thread.currentThread() )
        {
            throw new RuntimeException( whyFailed );
        }
        theTestPassed = false;
        testGeneratedInterrupt = true;
        failureMessage = whyFailed;
        mainThread.interrupt();
    }
}
class TestPassedException extends RuntimeException
{
}
class Sysout
{
    private static TestDialog dialog;
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
        System.out.println(messageIn);
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

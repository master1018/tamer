public class WindowOpacity
{
    private static void realSync() {
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
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
        if (!AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.TRANSLUCENT)) {
            System.out.println("Either the Toolkit or the native system does not support controlling the window opacity level.");
            pass();
        }
        boolean passed;
        Frame f = new Frame("Opacity test");
        f.setUndecorated(true);
        passed = false;
        try {
            AWTUtilities.getWindowOpacity(null);
        } catch (NullPointerException e) {
            passed = true;
        }
        if (!passed) {
            fail("getWindowOpacity() allows passing null.");
        }
        passed = false;
        try {
            AWTUtilities.setWindowOpacity(null, 0.5f);
        } catch (NullPointerException e) {
            passed = true;
        }
        if (!passed) {
            fail("setWindowOpacity() allows passing null.");
        }
        float curOpacity = AWTUtilities.getWindowOpacity(f);
        if (curOpacity < 1.0f || curOpacity > 1.0f) {
            fail("getWindowOpacity() reports the initial opacity level other than 1.0: " + curOpacity);
        }
        passed = false;
        try {
            AWTUtilities.setWindowOpacity(f, -0.5f);
        } catch (IllegalArgumentException e) {
            passed = true;
        }
        if (!passed) {
            fail("setWindowOpacity() allows passing negative opacity level.");
        }
        passed = false;
        try {
            AWTUtilities.setWindowOpacity(f, 1.5f);
        } catch (IllegalArgumentException e) {
            passed = true;
        }
        if (!passed) {
            fail("setWindowOpacity() allows passing opacity level greater than 1.0.");
        }
        AWTUtilities.setWindowOpacity(f, 0.5f);
        curOpacity = AWTUtilities.getWindowOpacity(f);
        if (curOpacity < 0.5f || curOpacity > 0.5f) {
            fail("getWindowOpacity() reports the opacity level that differs from the value set with setWindowOpacity: " + curOpacity);
        }
        AWTUtilities.setWindowOpacity(f, 0.75f);
        curOpacity = AWTUtilities.getWindowOpacity(f);
        if (curOpacity < 0.75f || curOpacity > 0.75f) {
            fail("getWindowOpacity() reports the opacity level that differs from the value set with setWindowOpacity the second time: " + curOpacity);
        }
        f.setBounds(100, 100, 300, 200);
        f.setVisible(true);
        realSync();
        curOpacity = AWTUtilities.getWindowOpacity(f);
        if (curOpacity < 0.75f || curOpacity > 0.75f) {
            fail("getWindowOpacity() reports the opacity level that differs from the value set with setWindowOpacity before showing the frame: " + curOpacity);
        }
        AWTUtilities.setWindowOpacity(f, 0.5f);
        realSync();
        curOpacity = AWTUtilities.getWindowOpacity(f);
        if (curOpacity < 0.5f || curOpacity > 0.5f) {
            fail("getWindowOpacity() reports the opacity level that differs from the value set with setWindowOpacity after showing the frame: " + curOpacity);
        }
        WindowOpacity.pass();
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

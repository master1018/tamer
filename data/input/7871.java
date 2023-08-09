public class EmbeddedFrameTest1
{
    private static void init()
    {
        String[] instructions = {
            "This is an AUTOMATIC test, simply wait until it is done.",
            "The result (passed or failed) will be shown in the",
            "message window below."
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );
        SunToolkit tk = (SunToolkit) Toolkit.getDefaultToolkit();
        if ("sun.awt.motif.MToolkit".equals(tk.getClass().getName())) {
            System.out.println("We shouldn't test MToolkit.");
            EmbeddedFrameTest1.pass();
            return;
        }
        try {
            final Frame frame = new Frame("AWT Frame");
            frame.pack();
            frame.setSize(200,200);
            final Frame embedded_frame = Util.createEmbeddedFrame(frame);
            embedded_frame.setSize(200, 200);
            Sysout.println("embedded_frame = " + embedded_frame);
            final JPopupMenu menu = new JPopupMenu();
            JButton item = new JButton("A button in popup");
            item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Button pressed");
                    }
                });
            menu.add(item);
            final JButton btn = new JButton("Press me to see popup");
            btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        menu.show(btn, 0, btn.getHeight());
                    }
                });
            final Panel p = new Panel();
            p.setLayout(new BorderLayout());
            embedded_frame.add(p,BorderLayout.CENTER);
            embedded_frame.validate();
            p.add(btn);
            p.validate();
            frame.setVisible(true);
            Robot robot = new Robot();
            tk.realSync();
            Util.clickOnComp(btn, robot);
            tk.realSync();
            Util.clickOnComp(item, robot);
            tk.realSync();
            if (item.getMousePosition() == null) {
                throw new RuntimeException("Popup was not closed (mouse above it)");
            }
            embedded_frame.remove(p);
            embedded_frame.dispose();
            frame.dispose();
        } catch (Throwable thr) {
            thr.printStackTrace();
            EmbeddedFrameTest1.fail("TEST FAILED: " + thr);
        }
        EmbeddedFrameTest1.pass();
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

public class OpensWithNoGrab
{
    final static int delay = 50;
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
        String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
        if (toolkit.equals("sun.awt.windows.WToolkit")){
            System.out.println("This test is for XAWT/Motif only");
            OpensWithNoGrab.pass();
        }
        Choice ch = new Choice ();
        Frame f = new Frame ("OpensWithNoGrab");
        Robot robot;
        Point framePt, choicePt;
        ch.add("line 1");
        ch.add("line 2");
        ch.add("line 3");
        ch.add("line 4");
        ch.setBackground(Color.red);
        f.add(ch);
        Menu file = new Menu ("file");
        MenuBar mb = new MenuBar();
        mb.add(file);
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        file.add(new MenuItem ("            "));
        f.setMenuBar(mb);
        f.setBackground(Color.green);
        f.setForeground(Color.green);
        f.setSize(300, 200);
        f.setVisible(true);
        try {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(50);
            Util.waitForIdle(robot);
            choicePt = ch.getLocationOnScreen();
            robot.mouseMove(choicePt.x + ch.getWidth()/2, choicePt.y + ch.getHeight()/2);
            robot.delay(delay);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(delay);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(delay);
            framePt = f.getLocationOnScreen();
            robot.mouseMove(choicePt.x + 10, choicePt.y - 15);
            robot.delay(10*delay);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(delay);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(delay);
            robot.mouseMove(choicePt.x + 15, choicePt.y + 15);
            Util.waitForIdle(robot);
            Color c = robot.getPixelColor(choicePt.x + 15, choicePt.y + 15);
            System.out.println("Color obtained under opened menu is: "+c );
            if (!c.equals(Color.red)){
                OpensWithNoGrab.fail("Failed: menu was opened by first click after opened Choice.");
            }
        }catch(Exception e){
            e.printStackTrace();
            OpensWithNoGrab.fail("Failed: exception occur "+e);
        }
        OpensWithNoGrab.pass();
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

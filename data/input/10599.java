public class GrabSequence
{
    private static void init()
    {
        String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
        if ( toolkit.equals("sun.awt.motif.MToolkit")){
            System.out.println("This test is for XToolkit and WToolkit only. Now using " + toolkit + ". Automatically passed.");
            return;
        }
        Sysout.createDialog();
        Frame frame = new Frame("Frame");
        frame.setBackground(Color.green);
        frame.setForeground(Color.green);
        frame.setLayout(new FlowLayout());
        Choice ch = new Choice();
        ch.setBackground(Color.red);
        ch.setForeground(Color.red);
        ch.addItem("One");
        ch.addItem("Two");
        ch.addItem("Three");
        ch.addItem("Four");
        ch.addItem("Five");
        final PopupMenu pm = new PopupMenu();
        MenuItem i1 = new MenuItem("Item1");
        MenuItem i2 = new MenuItem("Item2");
        MenuItem i3 = new MenuItem("Item3");
        pm.add(i1);
        pm.add(i2);
        pm.add(i3);
        ch.add(pm);
        ch.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    System.out.println("mousePressed"+event);
                    pm.show((Choice)event.getSource(), event.getX(), event.getY());
                }
            }
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    System.out.println("mouseReleased"+event);
                    pm.show((Choice)event.getSource(), event.getX(), event.getY());
                }
            }
        });
        frame.add(ch);
        frame.setSize(200, 200);
        frame.setVisible(true);
        try {
            Robot robot = new Robot();
            Util.waitForIdle(robot);
            robot.mouseMove(ch.getLocationOnScreen().x + ch.getWidth()/2,
                            ch.getLocationOnScreen().y + ch.getHeight()/2 );
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(100);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            Util.waitForIdle(robot);
            Color color = robot.getPixelColor(ch.getLocationOnScreen().x + ch.getWidth()/2,
                                              ch.getLocationOnScreen().y + ch.getHeight()*4);
            System.out.println("1. Color is " + color);
            if (!color.equals(Color.red)){
                GrabSequence.fail("Test failed. Choice is still not opened. ");
            }
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.delay(100);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            Util.waitForIdle(robot);
            color = robot.getPixelColor(ch.getLocationOnScreen().x + ch.getWidth()/2,
                                              ch.getLocationOnScreen().y + ch.getHeight()*2);
            System.out.println("2. Color is " + color);
            if (color.equals(Color.green)){
                GrabSequence.fail("Test failed. popup menu is not opened. ");
            }
            color = robot.getPixelColor(ch.getLocationOnScreen().x + ch.getWidth()/2,
                                              ch.getLocationOnScreen().y + ch.getHeight()*4);
            System.out.println("3. Color is " + color);
            if (!color.equals(Color.green) && !Toolkit.getDefaultToolkit().getClass().getName().equals("sun.awt.windows.WToolkit")){
                GrabSequence.fail("Test failed. Choice is still opened. ");
            }
        } catch(AWTException e){
            GrabSequence.fail("Test interrupted."+e);
        }
        GrabSequence.pass();
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

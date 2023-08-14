public class MouseAdapterUnitTest
{
    static Point pt;
    static Frame frame = new Frame("Test Frame");
    static Button b = new Button("Test Button");
    static Robot robot;
    static boolean clicked = false;
    static boolean pressed = false;
    static boolean released = false;
    static boolean entered = false;
    static boolean exited = false;
    static boolean rotated = false;
    static boolean dragged = false;
    static boolean moved = false;
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
        MouseAdapter ma = new MouseAdapter(){
                public void mouseClicked(MouseEvent e) {clicked = true;}
                public void mousePressed(MouseEvent e) { pressed = true;}
                public void mouseReleased(MouseEvent e) {released = true;}
                public void mouseEntered(MouseEvent e) { entered = true;}
                public void mouseExited(MouseEvent e) {exited  = true;}
                public void mouseWheelMoved(MouseWheelEvent e){rotated = true;}
                public void mouseDragged(MouseEvent e){dragged = true;}
                public void mouseMoved(MouseEvent e){moved = true;}
            };
        b.addMouseListener(ma);
        b.addMouseWheelListener(ma);
        b.addMouseMotionListener(ma);
        frame.add(b);
        frame.pack();
        frame.setVisible(true);
        try{
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(50);
            Util.waitForIdle(robot);
            pt = b.getLocationOnScreen();
            testPressMouseButton(InputEvent.BUTTON1_MASK);
            testDragMouseButton(InputEvent.BUTTON1_MASK);
            testMoveMouseButton();
            testCrossingMouseButton();
            testWheelMouseButton();
        } catch (Throwable e) {
            throw new RuntimeException("Test failed. Exception thrown: "+e);
        }
        MouseAdapterUnitTest.pass();
    }
    public static void testPressMouseButton(int button){
        robot.mouseMove(pt.x + b.getWidth()/2, pt.y + b.getHeight()/2);
        robot.delay(100);
        robot.mousePress(button);
        robot.mouseRelease(button);
        robot.delay(300);
        if ( !pressed || !released || !clicked ){
            dumpListenerState();
            fail("press, release or click hasn't come");
        }
    }
    public static void testWheelMouseButton(){
        robot.mouseMove(pt.x + b.getWidth()/2, pt.y + b.getHeight()/2);
        robot.mouseWheel(10);
        if ( !rotated){
            dumpListenerState();
            fail("Wheel event hasn't come");
        }
    }
    public static void testDragMouseButton(int button) {
        robot.mouseMove(pt.x + b.getWidth()/2, pt.y + b.getHeight()/2);
        robot.mousePress(button);
        moveMouse(pt.x + b.getWidth()/2, pt.y +
                  b.getHeight()/2,
                  pt.x + b.getWidth()/2,
                  pt.y + 2 * b.getHeight());
        robot.mouseRelease(button);
        if ( !dragged){
            dumpListenerState();
            fail("dragged hasn't come");
        }
    }
    public static void testMoveMouseButton() {
        moveMouse(pt.x + b.getWidth()/2, pt.y +
                  b.getHeight()/2,
                  pt.x + b.getWidth()/2,
                  pt.y + 2 * b.getHeight());
        if ( !moved){
            dumpListenerState();
            fail("dragged hasn't come");
        }
    }
    public static void moveMouse(int x0, int y0, int x1, int y1){
        int curX = x0;
        int curY = y0;
        int dx = x0 < x1 ? 1 : -1;
        int dy = y0 < y1 ? 1 : -1;
        while (curX != x1){
            curX += dx;
            robot.mouseMove(curX, curY);
        }
        while (curY != y1 ){
            curY += dy;
            robot.mouseMove(curX, curY);
        }
    }
    public static void testCrossingMouseButton() {
        moveMouse(pt.x + b.getWidth()/2,
                  pt.y + b.getHeight()/2,
                  pt.x + b.getWidth()/2,
                  pt.y + 2 * b.getHeight());
        moveMouse(pt.x + b.getWidth()/2,
                  pt.y + 2 * b.getHeight()/2,
                  pt.x + b.getWidth()/2,
                  pt.y + b.getHeight());
        if ( !entered || !exited){
            dumpListenerState();
            fail("enter or exit hasn't come");
        }
    }
    public static void dumpListenerState(){
        System.out.println("pressed = "+pressed);
        System.out.println("released = "+released);
        System.out.println("clicked = "+clicked);
        System.out.println("entered = "+exited);
        System.out.println("rotated = "+rotated);
        System.out.println("dragged = "+dragged);
        System.out.println("moved = "+moved);
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

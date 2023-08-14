public class SetBackgroundTest
{
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
        test();
        SetBackgroundTest.pass();
    }
    private static boolean isXAWT = (Toolkit.getDefaultToolkit().getClass().getName().equals("sun.awt.X11.XToolkit"));
    private static Robot robot = null;
    private static Frame frame = null;
    private static final Color color = Color.red;
    private static Color roughColor = null;
    private static void initRoughColor(){
        Canvas canvas = new Canvas();
        canvas.setBackground(color);
        frame.add(canvas, BorderLayout.CENTER);
        frame.validate();
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Point loc = canvas.getLocationOnScreen();
        Color robotColor = robot.getPixelColor(loc.x + canvas.getWidth()/2, loc.y + canvas.getHeight()/2);
        roughColor = robotColor;
        Sysout.println(" --- init rough color ... ");
        Sysout.println("     color = "+color);
        Sysout.println("     roughColor = "+roughColor);
        frame.remove(canvas);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
    }
    private static void test() {
        if (!isXAWT){
            Sysout.println(" this is XAWT-only test. ");
            return;
        }
        frame = new Frame();
        frame.setBounds(400,400,200,200);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        try{
            robot = new Robot();
        }catch(AWTException e){
            throw new RuntimeException(e.getMessage());
        }
        initRoughColor();
        Component[] components = new Component[] {
            new Button(), new Checkbox(), new Label(), new List(3, false),
            new TextArea(), new TextField(), new Choice()
        };
        for (Component component : components) {
            testComponent(new Panel(), component, color);
        }
        frame.dispose();
    }
    private static void testComponent(Container container, Component component, Color color){
        component.setBackground(color);
        container.setLayout(new BorderLayout());
        container.add(component, BorderLayout.CENTER);
        frame.add(container, BorderLayout.CENTER);
        frame.add("Center", container);
        frame.validate();
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Point loc = component.getLocationOnScreen();
        Color robotColor = robot.getPixelColor(loc.x + component.getWidth()/2, loc.y + component.getHeight()/2);
        Sysout.println(" --- test ... ");
        Sysout.println("     container = "+container);
        Sysout.println("     component = "+component);
        Sysout.println("     color = "+color);
        Sysout.println("     roughColor = "+roughColor);
        Sysout.println("     robotColor = "+robotColor);
        if(robotColor.getRGB() != roughColor.getRGB()){
            throw new RuntimeException(" the case failed. ");
        } else {
            Sysout.println(" the case passed. ");
        }
        container.remove(component);
        frame.remove(container);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
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

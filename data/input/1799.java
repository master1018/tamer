public class SpuriousExitEnter_2 {
    static JFrame frame = new JFrame("SpuriousExitEnter_2(LW)");
    static JButton jbutton = new JButton("jbutton");
    static Frame frame1 = new Frame("SpuriousExitEnter_2 (HW)");
    static Button button1 = new Button("button");
    private static void init() {
        String[] instructions =
        {
            "You see a plain JFrame with JButton in it and Frame with Button in it.",
            " Following steps should be accomplished for both of them (for Frame and for JFrame).",
            " If any of the following ",
            " third steps fails, press Fail.",
            " Let A area is the area inside Button.",
            " Let B area is the area inside Frame but not inside Button.",
            " Let C area is the area outside Frame.",
            " Stage 1:",
            " 1) Put the mouse pointer into A area.",
            " 2) Press Alt+Space (or other sequence opening the system menu from the top-left corner) ",
            " and resize the Frame so the pointer remains in A.",
            " 3) Press Enter key. No event should be fired.",
            " Stage 2:",
            " Repeat Stage 1 with area B.",
            " Stage 3:",
            " Repeat Stage 1 with area C.",
            " Stage 4:",
            " 1) Put the mouse pointer into A area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in B.",
            " 3) Press Enter key. Exited event on Button and Entered event on Frame should be fired.",
            " Stage 5:",
            " 1) Put the mouse pointer into A area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in C.",
            " 3) Press Enter key. Exited event on Button should be fired.",
            " Stage 6:",
            " 1) Put the mouse pointer into B area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in A.",
            " 3) Press Enter key. Exited event on Frame and Entered event on Button should be fired.",
            " Stage 7:",
            " 1) Put the mouse pointer into B area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in C.",
            " 3) Press Enter key. Exited event on Frame should be fired.",
            " Stage 8:",
            " 1) Put the mouse pointer into C area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in A.",
            " 3) Press Enter key. Entered event on Button should be fired.",
            " Stage 9:",
            " 1) Put the mouse pointer into C area.",
            " 2) Press Alt+Space and resize the Frame so the pointer becomes in B.",
            " 3) Press Enter key. Entered event on Frame should be fired.",
            " Stage 10:",
            " Repeat Stages from 4 to 9 with using Move command rather then Resize. ",
            " Note, that when the pointer jumps to the titlebar when you select \"Move window\", ",
            " it is OK to fire Exited event. Then, if the pointer returns to component back, ",
            " it should fire Entered event.",
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );
        Sysout.enableNumbering(true);
        MouseAdapter enterExitAdapter = new MouseAdapter() {
                public void mouseEntered(MouseEvent e){
                    Sysout.println("Entered on " + e.getSource().getClass().getName());
                }
                public void mouseExited(MouseEvent e){
                    Sysout.println("Exited on " + e.getSource().getClass().getName());
                }
            };
        frame.addMouseListener(enterExitAdapter);
        jbutton.addMouseListener(enterExitAdapter);
        frame.setSize(600, 200);
        frame.add(jbutton, BorderLayout.NORTH);
        frame.setVisible(true);
        frame1.addMouseListener(enterExitAdapter);
        button1.addMouseListener(enterExitAdapter);
        frame1.setSize(600, 200);
        frame1.add(button1, BorderLayout.NORTH);
        frame1.setVisible(true);
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
        if (mainThread != null){
            mainThread.interrupt();
        }
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
    private static boolean numbering = false;
    private static int messageNumber = 0;
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
    public static void enableNumbering(boolean enable){
        numbering = enable;
    }
    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }
    public static void println( String messageIn )
    {
        if (numbering) {
            messageIn = "" + messageNumber + " " + messageIn;
            messageNumber++;
        }
        dialog.displayMessage( messageIn );
    }
}
class TestDialog extends Dialog implements ActionListener
{
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
    Panel  buttonP = new Panel();
    Button passB = new Button( "pass" );
    Button failB = new Button( "fail" );
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add( "North", instructionsText );
        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add("Center", messageText);
        passB = new Button( "pass" );
        passB.setActionCommand( "pass" );
        passB.addActionListener( this );
        buttonP.add( "East", passB );
        failB = new Button( "fail" );
        failB.setActionCommand( "fail" );
        failB.addActionListener( this );
        buttonP.add( "West", failB );
        add( "South", buttonP );
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
    public void actionPerformed( ActionEvent e )
    {
        if( e.getActionCommand() == "pass" )
        {
            SpuriousExitEnter_2.pass();
        }
        else
        {
            SpuriousExitEnter_2.fail();
        }
    }
}

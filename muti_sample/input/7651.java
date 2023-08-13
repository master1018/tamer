public class UpdatingBootTime extends Applet
{
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            "1) Press the mouse over the button.",
            "2) A two timestamps would be printed.",
            "3) Verify that they are not differ a lot: it is okay to observe a 1 or 2 seconds difference.",
            "4) Change the system time significantly(by a month or a year) by using the OS abilities.",
            "5) Click on the button once again.",
            "6) Printed times should still be the same. Pay attention to the Month/Year if they were changed.",
            "7) It is okay to observe a 1 or 2 seconds difference and this is not a fail.",
            "8) If the difference is more then 1-2 seconds noticed then the test fail; otherwise pass."
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    public void start ()
    {
        Button b = new Button("Press me");
        b.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    Sysout.println("Event occured : "+e);
                    Sysout.println("The event time is : "+ (new Date(e.getWhen())));
                    Sysout.println("The system time is : "+ (new Date()));
                }
            });
        add(b);
        setSize (200,200);
        setVisible(true);
        validate();
    }
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

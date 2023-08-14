public class HoveringAndDraggingTest extends java.applet.Applet {
    public void start() {
        String[] instructions = new String[] {
            "1. Notice components in test window: main-panel, box-for-text,"
                +" 2 scroll-sliders, and 4 scroll-buttons.",
            "2. Hover mouse over box-for-text."
                +" Make sure, that mouse cursor is TextCursor (a.k.a. \"beam\").",
            "3. Hover mouse over each of components (see item 1), except for box-for-text."
                +" Make sure, that cursor is DefaultCursor (arrow).",
            "4. Drag mouse (using any mouse button) from box-for-text to every"
                +" component in item 1, and also outside application window."
                +" Make sure, that cursor remains TextCursor while mouse button is pressed.",
            "5. Repeat item 4 for each other component in item 1, except for box-for-text,"
                +" _but_ now make sure that cursor is DefaultCursor.",
            "6. If cursor behaves as described in items 2-3-4-5, then test passed; otherwise it failed."
        };
        Sysout.createDialogWithInstructions( instructions );
        Panel panel = new Panel();
        panel.setLayout( new GridLayout(3,3) );
        for( int y=0; y<3; ++y ) {
            for( int x=0; x<3; ++x ) {
                if( x==1 && y==1 ) {
                    panel.add( new TextArea( bigString() ) );
                } else {
                    panel.add( new Panel() );
                }
            }
        }
        Frame frame = new Frame( "TextArea cursor icon test" );
        frame.setSize( 300, 300 );
        frame.add( panel );
        frame.setVisible( true );
    }
    static String bigString() {
        String s = "";
        for( int lines=0; ; ++lines ) {
            for( int symbols=0; symbols<100; ++symbols ) {
                s += "0";
            }
            if( lines<50 ) {
                s += "\n";
            } else {
                break;
            }
        }
        return s;
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

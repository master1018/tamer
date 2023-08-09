public class SetFontTest extends Applet
{
    List list = new List(8, false);
    Button button1 = new Button("Enlarge font");
    Button button2 = new Button("Change mode");
    public void init()
    {
        list.add("111");
        list.add("222");
        list.add("333");
        list.add("444");
        this.add(list);
        this.add(button1);
        this.add(button2);
        button1.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                    list.setFont( new Font( "SansSerif", Font.PLAIN, 30 ) );
                    list.repaint();
                }
            });
        button2.addActionListener(
            new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                    list.setMultipleMode(true);
                }
            });
        this.setLayout (new FlowLayout ());
        String[] instructions =
        {
            "1) Click on the 'Enlarge font' button to enlarge font of the list.",
            "2) If you see that the rows of the list overlap one another "+
            "then the test failed. Otherwise, goto to the step 3.",
            "3) Click on the 'Change mode' button to set multiple-selection mode.",
            "4) If you see that the rows of the list overlap one another "+
            "then the test failed. Otherwise, the test passed."
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    public void start ()
    {
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

public class MultipleMode extends Applet
{
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            " 1. Turn the 'multiple' checkbox off and press the 'open' button ",
            " 2. Verify that the file dialog doesn't allow the multiple file selection ",
            " 3. Select any file and close the file dialog ",
            " 4. The results will be displayed, verify the results ",
            " 5. Turn the 'multiple' checkbox on and press the 'open' button ",
            " 6. Verify that the file dialog allows the multiple file selection ",
            " 7. Select several files and close the file dialog ",
            " 8. The results will be displayed, verify the results "
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    public void start ()
    {
        final Checkbox mode = new Checkbox("multiple", true);
        Button open = new Button("open");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog d = new FileDialog((Frame)null);
                d.setMultipleMode(mode.getState());
                d.setVisible(true);
                Sysout.println("DIR:");
                Sysout.println(d.getDirectory());
                Sysout.println("FILE:");
                Sysout.println(d.getFile());
                Sysout.println("FILES:");
                File files[] = d.getFiles();
                for (File f : files) {
                    Sysout.println(String.valueOf(f));
                }
            }
        });
        setLayout(new FlowLayout());
        add(mode);
        add(open);
        setSize (200,200);
        setVisible(true);
        validate();
    }
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

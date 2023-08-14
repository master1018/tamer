public class FileDialogReturnTest extends Applet
{
    public static void main(String[] args) {
        Applet a = new FileDialogReturnTest();
        a.init();
        a.start();
    }
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            " 1. The test shows the 'FileDialogReturnTest' applet which contains two text fields and one button, ",
            " 2. Input something into the 'File:' text field or just keep the field empty, ",
            " 3. Input something into the 'Dir:' text field or just keep the field empty, ",
            " 4. Press the 'Show' button and a file dialog will appear, ",
            " 5-1. Cancel the file dialog, e.g. by selecting the 'close' menu item, ",
            "      If the output window shows that 'file'/'dir' values is null then the test passes, otherwise the test fails, ",
            " 5-2. Select any file, e.g. by pressing the 'OK' button, ",
            "      If the output window shows that 'file'/'dir' values is not-null then the test passes, otherwise the test fails. "
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    final TextField fileField = new TextField("", 20);
    final TextField dirField = new TextField("", 20);
    final Button button = new Button("Show");
    public void start ()
    {
        setLayout(new FlowLayout());
        add(new Label("File:"));
        add(fileField);
        add(new Label("Dir:"));
        add(dirField);
        add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDialog();
            }
        });
        setSize (200,200);
        setVisible(true);
        validate();
    }
    void showDialog()
    {
        FileDialog fd = new FileDialog(new Frame());
        fd.setFile(fileField.getText());
        fd.setDirectory(dirField.getText());
        fd.setVisible(true);
        Sysout.println("[file=" + fd.getFile()+"]");
        Sysout.println("[dir=" + fd.getDirectory()+"]");
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
    int maxStringLength = 100;
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

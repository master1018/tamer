public class PrintCheckboxManualTest extends Panel
{
    Frame f;
    public static void main(String[] args) {
        PrintCheckboxManualTest a = new PrintCheckboxManualTest();
        a.init();
        a.start();
    }
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            "Linux or Solaris with XToolkit ONLY!",
            "1. Click the 'Print' button on the frame",
            "2. Select a printer in the print dialog and proceed",
            "3. If the frame with checkbox and button on it is printed successfully test PASSED else FAILED"
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        f = new Frame("Print checkbox");
        f.setLayout(new GridLayout(2, 2));
        f.setSize(200, 100);
        Checkbox ch = new Checkbox("123");
        ch.setState(true);
        f.add(ch);
        Scrollbar sb = new Scrollbar(Scrollbar.HORIZONTAL);
        f.add(sb);
        Button b = new Button("Print");
        b.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent ev)
        {
                PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(f, "PrintCheckboxManualTest", null);
                if (pj != null)
                {
                        try
                        {
                                Graphics g = pj.getGraphics();
                                f.printAll(g);
                                g.dispose();
                                pj.end();
                                Sysout.println("Test PASSED");
                        }
                        catch (ClassCastException cce)
                        {
                                Sysout.println("Test FAILED: ClassCastException");
                        }
                        catch (Exception e)
                        {
                                Sysout.println("Test FAILED: unknown Exception");
                        }
                }
        }
        });
        f.add(b);
        f.setVisible(true);
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

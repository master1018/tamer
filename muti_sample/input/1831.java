public class PageSetupDlgBlockingTest extends Panel {
    public static Frame frame = new TestFrame("Test Frame");
    public static void main(String[] args) {
        PageSetupDlgBlockingTest a = new PageSetupDlgBlockingTest();
        a.init();
        a.start();
    }
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            "This test verifies that native modal 'Page Setup' dialog doesn't block event",
            "handling when called on EventDispatchThread.",
            " ",
            "After test started you will see 'Test Frame' frame which contains",
            "one 'Click Me' button.",
            "1. Click the button:",
            "   - 'Page Setup' dialog will appear.",
            "2. Drag the dialog over the 'Test Frame' so that to enforce its button redraw:",
            "   - if you're seeing the button redraw (as long as PAINT events are displayed)",
            "     the test PASSED else FAILED."
        };
        Sysout.createDialogWithInstructions(instructions);
    }
    public void start() {
        JButton button = new JButton("Click Me");
        final AWTEventListener listener = new AWTEventListener() {
                public void eventDispatched(AWTEvent e) {
                    if (e.getSource().getClass() == TestFrame.class) {
                        Sysout.println(e.paramString() + " on <Test Frame>");
                    }
                }
            };
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.PAINT_EVENT_MASK);
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.pageDialog(job.defaultPage());
                    Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
                }
            });
        button.setSize(100, 50);
        frame.setLayout(new BorderLayout());
        frame.setSize(200, 200);
        frame.setLocation(500, 0);
        frame.add(button, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
class TestFrame extends Frame {
    TestFrame(String title) {
        super(title);
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

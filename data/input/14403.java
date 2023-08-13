public class ButtonActionKeyTest extends Applet {
    Robot robot;
    JFrame frame = new JFrame("Frame");
    JButton button = new JButton("button");
    JTextField text = new JTextField("text");
    AtomicBoolean gotEvent = new AtomicBoolean(false);
    public static void main(String[] args) {
        ButtonActionKeyTest app = new ButtonActionKeyTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        this.setLayout (new BorderLayout ());
        Sysout.createDialogWithInstructions(new String[]
            {"This is an automatic test. Simply wait until it is done."
            });
    }
    public void start() {
        frame.setLayout(new FlowLayout());
        frame.add(button);
        frame.add(text);
        frame.pack();
        button.getInputMap().put(KeyStroke.getKeyStroke("A"), "GO!");
        button.getActionMap().put("GO!", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Sysout.println("Action performed!");
                text.requestFocusInWindow();
            }
        });
        text.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == 'a') {
                        Sysout.println(e.toString());
                        synchronized (gotEvent) {
                            gotEvent.set(true);
                            gotEvent.notifyAll();
                        }
                    }
                }
            });
        frame.setVisible(true);
        Util.waitForIdle(robot);
        Util.clickOnComp(button, robot);
        Util.waitForIdle(robot);
        if (!button.isFocusOwner()) {
            throw new Error("Test error: a button didn't gain focus.");
        }
        robot.keyPress(KeyEvent.VK_A);
        robot.delay(20);
        robot.keyRelease(KeyEvent.VK_A);
        if (Util.waitForCondition(gotEvent, 2000)) {
            throw new TestFailedException("an action key went into the text field!");
        }
        Sysout.println("Test passed.");
    }
}
class TestFailedException extends RuntimeException {
    TestFailedException(String msg) {
        super("Test failed: " + msg);
    }
}
class Sysout
{
    static TestDialog dialog;
    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        println( "Any messages for the tester will display here." );
    }
    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
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

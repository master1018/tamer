public class ClearLwQueueBreakTest extends Applet {
    JFrame f1 = new JFrame("frame");
    JFrame f2 = new JFrame("frame");
    JButton b = new JButton("button");
    JTextField tf1 = new JTextField("     ");
    JTextField tf2 = new JTextField("     ");
    JTextField tf3 = new JTextField("     ");
    AtomicBoolean typed = new AtomicBoolean(false);
    FocusListener listener1;
    FocusListener listener2;
    Robot robot;
    public static void main(String[] args) {
        ClearLwQueueBreakTest app = new ClearLwQueueBreakTest();
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
        b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    f2.setVisible(true);
                }
            });
        tf2.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '9') {
                        synchronized (typed) {
                            typed.set(true);
                            typed.notifyAll();
                        }
                    }
                }
            });
        tf3.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '8') {
                        synchronized (typed) {
                            typed.set(true);
                            typed.notifyAll();
                        }
                    }
                }
            });
        listener1 = new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    b.requestFocus();
                    tf1.requestFocus();
                    tf1.setFocusable(false);
                    tf2.requestFocus();
                }
            };
        listener2 = new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    b.requestFocus();
                    tf1.requestFocus();
                    tf2.requestFocus();
                    tf2.setFocusable(false);
                }
            };
        f1.add(b);
        f1.add(tf1);
        f1.add(tf2);
        f1.add(tf3);
        f1.setLayout(new FlowLayout());
        f1.pack();
        f1.setVisible(true);
        Util.waitForIdle(robot);
        f2.addFocusListener(listener1);
        Sysout.println("Stage 1.");
        test1();
        f2.removeFocusListener(listener1);
        f2.addFocusListener(listener2);
        Sysout.println("Stage 2.");
        test2();
        Sysout.println("Test passed.");
    }
    void test1() {
        Util.clickOnComp(b, robot);
        Util.waitForIdle(robot);
        if (!tf2.hasFocus()) {
            throw new TestFailedException("target component didn't get focus!");
        }
        robot.keyPress(KeyEvent.VK_9);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_9);
        synchronized (typed) {
            if (!Util.waitForCondition(typed, 2000)) {
                throw new TestFailedException("key char couldn't be typed!");
            }
        }
        Util.clickOnComp(tf3, robot);
        Util.waitForIdle(robot);
        if (!tf3.hasFocus()) {
            throw new Error("a text field couldn't be focused.");
        }
        typed.set(false);
        robot.keyPress(KeyEvent.VK_8);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_8);
        synchronized (typed) {
            if (!Util.waitForCondition(typed, 2000)) {
                throw new TestFailedException("key char couldn't be typed!");
            }
        }
    }
    void test2() {
        Util.clickOnComp(b, robot);
        Util.waitForIdle(robot);
        if (!b.hasFocus()) {
            throw new TestFailedException("focus wasn't restored correctly!");
        }
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

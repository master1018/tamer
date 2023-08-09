public class SubMenuShowTest extends Applet {
    Robot robot;
    JFrame frame = new JFrame("Test Frame");
    JMenuBar bar = new JMenuBar();
    JMenu menu = new JMenu("Menu");
    JMenu submenu = new JMenu("More");
    JMenuItem item = new JMenuItem("item");
    AtomicBoolean activated = new AtomicBoolean(false);
    public static void main(String[] args) {
        SubMenuShowTest app = new SubMenuShowTest();
        app.init();
        app.start();
    }
    public void init() {
        robot = Util.createRobot();
        this.setLayout (new BorderLayout ());
        Sysout.createDialogWithInstructions(new String[]
            {"This is a manual test. Simple wait until it is done."
            });
    }
    public void start() {
        menu.setMnemonic('f');
        submenu.setMnemonic('m');
        menu.add(submenu);
        submenu.add(item);
        bar.add(menu);
        frame.setJMenuBar(bar);
        frame.pack();
        item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Sysout.println(e.toString());
                    synchronized (activated) {
                        activated.set(true);
                        activated.notifyAll();
                    }
                }
            });
        frame.setVisible(true);
        Util.waitForIdle(robot);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.delay(20);
        robot.keyPress(KeyEvent.VK_F);
        robot.delay(20);
        robot.keyRelease(KeyEvent.VK_F);
        robot.delay(20);
        robot.keyRelease(KeyEvent.VK_ALT);
        Util.waitForIdle(robot);
        robot.keyPress(KeyEvent.VK_M);
        robot.delay(20);
        robot.keyRelease(KeyEvent.VK_M);
        Util.waitForIdle(robot);
        robot.keyPress(KeyEvent.VK_SPACE);
        robot.delay(20);
        robot.keyRelease(KeyEvent.VK_SPACE);
        Util.waitForIdle(robot);
        if (!Util.waitForCondition(activated, 2000)) {
            throw new TestFailedException("a submenu wasn't activated by mnemonic key press");
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

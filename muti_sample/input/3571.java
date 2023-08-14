public class AcceleratorTest extends JApplet
{
    static int pressed = 0;
    Hashtable<String, Integer> cmdHash = new Hashtable<String, Integer>();
    String[] CMD = {
        "\u042E, keep me in focus",
        "Item Cyrl Be",
        "Item English Period",
        "Item English N",
        "\u0436"
    };
    JFrame jfr;
    public void init()
    {
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            " Ensure you have Russian keyboard layout as a currently active.",
            "(1) Press Ctrl+\u0411 (a key with \",<\" on it) ",
            "(2) Find a . (period) in this layout (perhaps \"/?\" or \"7&\" key).",
            "Press Ctrl+.",
            "(3) Press Crtl+ regular English . (period) key (on \".>\" )",
            "(4) Press Ctrl+ key with English N.",
            "(5) Press Alt+\u042E (key with \".>\")",
            "(6) Press Alt+\u0436 (key with \";:\")",
            "If all expected commands will be fired, look for message",
            "\"All tests passed\""
        };
        Sysout.createDialogWithInstructions( instructions );
        for(int i = 0; i < CMD.length; i++) {
            cmdHash.put(CMD[i], 0);
        }
        jfr = new JFrame();
        JButton jbu;
        jfr.add((jbu = new JButton(CMD[0])));
        jbu.setMnemonic(java.awt.event.KeyEvent.getExtendedKeyCodeForChar('\u042E'));
        jbu.addActionListener( new ALi(CMD[0]));
        JMenuBar menuBar = new JMenuBar();
        jfr.setJMenuBar(menuBar);
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        JMenuItem menuItem = new JMenuItem(CMD[1]);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.getExtendedKeyCodeForChar('\u0431'),
                        InputEvent.CTRL_DOWN_MASK));
        JMenuItem menuItemEnglish = new JMenuItem(CMD[2]);
        menuItemEnglish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
                        InputEvent.CTRL_DOWN_MASK));
        JMenuItem menuItemE1 = new JMenuItem(CMD[3]);
        menuItemE1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                        InputEvent.CTRL_DOWN_MASK));
        menuItem.addActionListener( new ALi(CMD[1]));
        menuItemEnglish.addActionListener( new ALi(CMD[2]));
        menuItemE1.addActionListener( new ALi(CMD[3]));
        menu.add(menuItem);
        menu.add(menuItemEnglish);
        menu.add(menuItemE1);
        KeyStroke ks;
        InputMap im = new InputMap();
        ks = KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar('\u0436'), java.awt.event.InputEvent.ALT_DOWN_MASK);
        im.put(ks, "pushAction");
        im.setParent(jbu.getInputMap(JComponent.WHEN_FOCUSED));
        jbu.setInputMap(JComponent.WHEN_FOCUSED, im);
        jbu.getActionMap().put("pushAction",
            new AbstractAction("pushAction") {
                  public void actionPerformed(ActionEvent evt) {
                      if( evt.getActionCommand().equals(CMD[4])) {
                          cmdHash.put(CMD[4], 1);
                      }
                      boolean notYet = false;
                      for(int i = 0; i < CMD.length; i++) {
                          if(cmdHash.get(CMD[i]) == 0 ) notYet = true;
                      }
                      Sysout.println("Fired");
                      if( !notYet ) {
                          Sysout.println("All tests passed.");
                      }
                  }
            }
        );
        jfr.setBounds(650,0,200,200);
        jfr.setVisible(true);
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
    }
    public class ALi implements ActionListener {
        String expectedCmd;
        public ALi( String eCmd ) {
            expectedCmd = eCmd;
        }
        public void actionPerformed(ActionEvent ae) {
            if( cmdHash.containsKey(ae.getActionCommand()) ) {
                cmdHash.put(expectedCmd, 1);
            }
            boolean notYet = false;
            for(int i = 0; i < CMD.length; i++) {
                if(cmdHash.get(CMD[i]) == 0 ) notYet = true;
            }
            Sysout.println("FIRED");
            if( !notYet ) {
                Sysout.println("All tests passed.");
            }
        }
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

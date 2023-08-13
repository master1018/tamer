public class CtrlASCII extends Applet implements KeyListener
{
    static Hashtable<Character, Integer> keycharHash = new Hashtable<Character, Integer>();
    static boolean testFailed = false;
    TextField tf;
    Robot robot;
    static void fillHash( boolean isMSWindows ) {
        keycharHash.put(    (char)0x20         , KeyEvent.VK_SPACE        );                       
        keycharHash.put(    (char)0x21         , KeyEvent.VK_EXCLAMATION_MARK        );            
        keycharHash.put(    (char)0x22         , KeyEvent.VK_QUOTEDBL        );                    
        keycharHash.put(    (char)0x23         , KeyEvent.VK_NUMBER_SIGN        );                 
        keycharHash.put(    (char)0x24         , KeyEvent.VK_DOLLAR        );                       
        keycharHash.put(    (char)0x26    , KeyEvent.VK_AMPERSAND        );                   
        keycharHash.put(    (char)0x27    , KeyEvent.VK_QUOTE        );                       
        keycharHash.put(    (char)0x28    , KeyEvent.VK_LEFT_PARENTHESIS        );            
        keycharHash.put(    (char)0x29    , KeyEvent.VK_RIGHT_PARENTHESIS        );            
        keycharHash.put(    (char)0x2a    , KeyEvent.VK_ASTERISK        );                     
        keycharHash.put(    (char)0x2b    , KeyEvent.VK_PLUS        );                         
        keycharHash.put(    (char)0x2c    , KeyEvent.VK_COMMA        );                         
        keycharHash.put(    (char)0x2d    , KeyEvent.VK_MINUS        );                        
        keycharHash.put(    (char)0x2e    , KeyEvent.VK_PERIOD        );                       
        keycharHash.put(    (char)0x2f    , KeyEvent.VK_SLASH        );                        
        keycharHash.put(    (char)0x30    , KeyEvent.VK_0        );                           
        keycharHash.put(    (char)0x31    , KeyEvent.VK_1        );                           
        keycharHash.put(    (char)0x32    , KeyEvent.VK_2        );                           
        keycharHash.put(    (char)0x33    , KeyEvent.VK_3        );                           
        keycharHash.put(    (char)0x34    , KeyEvent.VK_4        );                           
        keycharHash.put(    (char)0x35    , KeyEvent.VK_5        );                           
        keycharHash.put(    (char)0x36    , KeyEvent.VK_6        );                           
        keycharHash.put(    (char)0x37    , KeyEvent.VK_7        );                           
        keycharHash.put(    (char)0x38    , KeyEvent.VK_8        );                           
        keycharHash.put(    (char)0x39    , KeyEvent.VK_9        );                           
        keycharHash.put(    (char)0x3a    , KeyEvent.VK_COLON        );                        
        keycharHash.put(    (char)0x3b    , KeyEvent.VK_SEMICOLON        );                    
        keycharHash.put(    (char)0x3c    , KeyEvent.VK_LESS        );                         
        keycharHash.put(    (char)0x3d    , KeyEvent.VK_EQUALS        );                      
        keycharHash.put(    (char)0x3e    , KeyEvent.VK_GREATER        );                      
        keycharHash.put(    (char)0x40   , KeyEvent.VK_AT        );                           
        keycharHash.put(    (char)0x1    , KeyEvent.VK_A        );                             
        keycharHash.put(    (char)0x2    , KeyEvent.VK_B        );                            
        keycharHash.put(    (char)0x3    , KeyEvent.VK_C        );                            
        keycharHash.put(    (char)0x4    , KeyEvent.VK_D        );                            
        keycharHash.put(    (char)0x5    , KeyEvent.VK_E        );                            
        keycharHash.put(    (char)0x6    , KeyEvent.VK_F        );                            
        keycharHash.put(    (char)0x7    , KeyEvent.VK_G        );                            
        keycharHash.put(    (char)0x8    , KeyEvent.VK_H        );                            
        keycharHash.put(    (char)0x9    , KeyEvent.VK_I        );                            
        keycharHash.put(    (char)0xa    , KeyEvent.VK_J        );                            
        keycharHash.put(    (char)0xb    , KeyEvent.VK_K        );                            
        keycharHash.put(    (char)0xc    , KeyEvent.VK_L        );                            
        keycharHash.put(    (char)0xd    , KeyEvent.VK_M        );                            
        keycharHash.put(    (char)0xe    , KeyEvent.VK_N        );                            
        keycharHash.put(    (char)0xf    , KeyEvent.VK_O        );                            
        keycharHash.put(    (char)0x10   , KeyEvent.VK_P        );                           
        keycharHash.put(    (char)0x11   , KeyEvent.VK_Q        );                           
        keycharHash.put(    (char)0x12   , KeyEvent.VK_R        );                           
        keycharHash.put(    (char)0x13   , KeyEvent.VK_S        );                           
        keycharHash.put(    (char)0x14   , KeyEvent.VK_T        );                           
        keycharHash.put(    (char)0x15   , KeyEvent.VK_U        );                           
        keycharHash.put(    (char)0x16   , KeyEvent.VK_V        );                           
        keycharHash.put(    (char)0x17   , KeyEvent.VK_W        );                           
        keycharHash.put(    (char)0x18   , KeyEvent.VK_X        );                           
        keycharHash.put(    (char)0x19   , KeyEvent.VK_Y        );                           
        keycharHash.put(    (char)0x1a   , KeyEvent.VK_Z        );                           
        keycharHash.put(    (char)0x1b   , KeyEvent.VK_OPEN_BRACKET        );              
        keycharHash.put(    (char)0x1c   , KeyEvent.VK_BACK_SLASH        );                
        keycharHash.put(    (char)0x1d   , KeyEvent.VK_CLOSE_BRACKET        );             
        keycharHash.put(    (char)0x5e   , KeyEvent.VK_CIRCUMFLEX        );                 
        keycharHash.put(    (char)0x1f   , KeyEvent.VK_UNDERSCORE        );                 
        keycharHash.put(    (char)0x60   , KeyEvent.VK_BACK_QUOTE        );               
        keycharHash.put(    (char)0x7b      , KeyEvent.VK_BRACELEFT        );              
        keycharHash.put(    (char)0x7d      , KeyEvent.VK_BRACERIGHT        );             
    }
    public static void main(String[] args) {
        CtrlASCII test = new CtrlASCII();
        test.init();
        test.start();
    }
    public void init()
    {
        fillHash( false );
        this.setLayout (new BorderLayout ());
        String[] instructions =
        {
            "This is an AUTOMATIC test",
            "simply wait until it is done"
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );
    }
    public void start ()
    {
        setSize(400,300);
        setVisible(true);
        String original = "0123456789";
        tf = new TextField(original, 20);
        this.add(tf);
        tf.addKeyListener(this);
        validate();
        try {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
            robot.setAutoDelay(100);
            robot.waitForIdle();
            robot.delay(2000);
            this.requestFocus();
            tf.requestFocusInWindow();
            Point pt = getLocationOnScreen();
            robot.mouseMove( pt.x+100, pt.y+100 );
            robot.delay(2000);
            robot.mousePress( InputEvent.BUTTON1_MASK );
            robot.mouseRelease( InputEvent.BUTTON1_MASK );
            Enumeration<Integer> enuElem = keycharHash.elements();
            int kc;
            while( enuElem.hasMoreElements()) {
                kc = enuElem.nextElement();
                punchCtrlKey( robot, kc );
            }
            robot.delay(500);
        } catch (Exception e) {
            throw new RuntimeException("The test was not completed.\n\n" + e);
        }
        if( testFailed ) {
            throw new RuntimeException("The test failed.\n\n");
        }
        Sysout.println("Success\n");
    }
    public void punchCtrlKey( Robot ro, int keyCode ) {
        ro.keyPress(KeyEvent.VK_CONTROL);
        ro.keyPress(keyCode);
        ro.keyRelease(keyCode);
        ro.keyRelease(KeyEvent.VK_CONTROL);
        ro.delay(200);
    }
    public void keyPressed(KeyEvent evt)
    {
    }
    public void keyTyped(KeyEvent evt)
    {
        printKey(evt);
        char keych = evt.getKeyChar();
        if( !keycharHash.containsKey( keych ) ) {
            System.out.println("Unexpected keychar: "+keych);
            Sysout.println("Unexpected keychar: "+keych);
            testFailed = true;
        }
    }
    public void keyReleased(KeyEvent evt)
    {
    }
    protected void printKey(KeyEvent evt)
    {
        switch(evt.getID())
        {
          case KeyEvent.KEY_TYPED:
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
            break;
          default:
            System.out.println("Other Event ");
            Sysout.println("Other Event ");
            return;
        }
        System.out.print(" 0x"+ Integer.toHexString(evt.getKeyChar()));
        Sysout.println    (" 0x"+ Integer.toHexString(evt.getKeyChar()));
    }
}
class Sysout
 {
   private static TestDialog dialog;
   public static void createDialogWithInstructions( String[] instructions )
    {
      dialog = new TestDialog( new Frame(), "Instructions" );
      dialog.printInstructions( instructions );
      dialog.show();
      println( "Any messages for the tester will display here." );
    }
   public static void createDialog( )
    {
      dialog = new TestDialog( new Frame(), "Instructions" );
      String[] defInstr = { "Instructions will appear here. ", "" } ;
      dialog.printInstructions( defInstr );
      dialog.show();
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
      add("South", messageText);
      pack();
      show();
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
    }
 }

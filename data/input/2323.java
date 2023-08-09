public class ToFrontFocus extends Applet
 {
     Frame cover, focus_frame, nonfocus_frame;
     Button focus_button, nonfocus_button;
     volatile boolean focus_gained = false, nonfocus_gained = false;
   public void init()
    {
      this.setLayout (new BorderLayout ());
      String[] instructions =
       {
           "This is an AUTOMATIC test",
           "simply wait until it is done"
       };
      Sysout.createDialog( );
      Sysout.printInstructions( instructions );
      cover = new Frame("Cover frame");
      cover.setBounds(100, 100, 200, 200);
      focus_frame = new Frame("Focusable frame");
      focus_frame.setBounds(150, 100, 250, 150);
      nonfocus_frame = new Frame("Non-focusable frame");
      nonfocus_frame.setFocusableWindowState(false);
      nonfocus_frame.setBounds(150, 150, 250, 200);
      focus_button = new Button("Button in focusable frame");
      focus_button.addFocusListener(new FocusAdapter() {
              public void focusGained(FocusEvent e) {
                  focus_gained = true;
              }
          });
      nonfocus_button = new Button("Button in non-focusable frame");
      nonfocus_button.addFocusListener(new FocusAdapter() {
              public void focusGained(FocusEvent e) {
                  nonfocus_gained = true;
              }
          });
    }
   public void start ()
    {
      setSize (200,200);
      show();
      Util.waitForIdle(null);
      focus_frame.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
              public Component getInitialComponent(Window w) {
                  return null;
              }
          });
      focus_frame.setVisible(true);
      nonfocus_frame.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
              public Component getInitialComponent(Window w) {
                  return null;
              }
          });
      nonfocus_frame.setVisible(true);
      cover.setVisible(true);
      Util.waitForIdle(null);
      focus_frame.add(focus_button);
      focus_frame.pack();
      focus_frame.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
              public Component getInitialComponent(Window w) {
                  return focus_button;
              }
          });
      nonfocus_frame.add(nonfocus_button);
      nonfocus_frame.pack();
      nonfocus_frame.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
              public Component getInitialComponent(Window w) {
                  return nonfocus_button;
              }
          });
      System.err.println("------------ Starting test ------------");
      focus_frame.toFront();
      nonfocus_frame.toFront();
      Util.waitForIdle(null);
      if (!focus_gained || nonfocus_gained) {
          throw new RuntimeException("ToFront doesn't work as expected");
      }
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

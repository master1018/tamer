public class ScrollSelectionTest extends Applet
 {
   Frame frame = new Frame("ScrollSelectionTest frame");
   TextField tf = new TextField(40);
   public void init()
    {
      tf.setText("abcdefghijklmnopqrstuvwxyz");
      frame.add(tf);
      tf.select(0, 20);
      String[] instructions = {
          "INSTRUCTIONS:",
          "This is a test for a win32 specific problem",
         "If you see all the letters from 'a' to 'z' and",
          "letters from 'a' to 't' are selected then test passes.",
          "You may have to activate the frame to see the selection"
          + " highlighted (e.g. by clicking on frame's title)."
      };
      Sysout.createDialogWithInstructions( instructions );
    }
   public void start ()
    {
      setSize (300,300);
      setVisible(true);
      frame.setVisible(true);
      frame.setBounds (400, 0, 300, 300);
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

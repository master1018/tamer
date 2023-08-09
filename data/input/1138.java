public class ConstructorsNullTest extends Applet
 {
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
    }
   public void start ()
    {
      setSize (200,200);
      show();
      ColorConvertOp gp;
      boolean passed = false;
      try {
          gp = new ColorConvertOp((ColorSpace)null, (RenderingHints)null);
      } catch (NullPointerException e) {
          try {
              gp = new ColorConvertOp((ColorSpace)null, null, null);
          } catch (NullPointerException e1) {
              try {
                  gp = new ColorConvertOp((ICC_Profile[])null, null);
              } catch (NullPointerException e2) {
                  passed = true;
              }
          }
      }
      if (!passed) {
          Sysout.println("Test FAILED: one of constructors didn't throw NullPointerException.");
          throw new RuntimeException("Test FAILED: one of constructors didn't throw NullPointerException.");
      }
      Sysout.println("Test PASSED: all constructors threw NullPointerException.");
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

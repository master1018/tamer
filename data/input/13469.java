public class DnDFileGroupDescriptor extends Applet {
    public void init() {
        setLayout(new BorderLayout());
        String[] instructions = {
         "The applet window contains a red field.",
         "1. Start MS Outlook program. Find and open ",
         "   the mail form with attachments.",
         "2. Select attachments from the mail and drag into a red field of applet.",
         "   When the mouse enters the field during the drag, the application ",
         "   should change the cursor form to OLE-copy and field color to yellow.",
         "3. Release the mouse button (drop attachments) over the field.",
         "",
         "File paths in temporary folder should appear.",
         "",
         "You should be able to repeat this operation multiple times.",
         "Please, select \"Pass\" just in case of success or \"Fail\" for another."
        };
        Sysout.createDialogWithInstructions( instructions );
    }
    public void start() {
        Panel   mainPanel;
        Component dropTarget;
        mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.blue);
        dropTarget = new DnDTarget(Color.red, Color.yellow);
        mainPanel.add(dropTarget, "Center");
        add(mainPanel);
        setSize(200,200);
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

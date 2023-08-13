public class DialogOrient implements Printable {
   private static void init() throws Exception {
        String[] instructions =
        {
         "To be able to run this test it is required to have a default",
         "printer configured in your user environment.",
         "",
         "Step 1. a page dialog will appear. Select LANDSCAPE orientation",
         "and OK the dialog.",
         "",
         "Step 2. Next a print dialog will appear. *IF* you can find",
         "an orientation setting on the Properties tab, confirm it",
         "still says LANDSCAPE. If it says PORTRAIT the test has failed.",
         "",
         "Step 3. If all is still correct, press OK. The test will now",
         "try to print. If it detects that it is being asked to print in",
         "portrait mode it will throw an Exception and the test has failed",
         "",
         "Step 4.  Collect the print out. Confirm that the orientation of",
         "the output is LANDSCAPE. If it is not the test fails",
         "",
         "Step 5. Repeat steps 2,3,4 one more time.",
         "Step 6. If everything has said LANDSCAPE up to this point,",
         "the test has passed.",
       };
      Sysout.createDialog( );
      Sysout.printInstructions( instructions );
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new DialogOrient());
        PageFormat landscape = job.pageDialog(job.defaultPage());
        if (job.printDialog()) {
            job.print();
        }
        if (job.printDialog()){
            job.print();
        }
    }
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        if (pf.getOrientation() != PageFormat.LANDSCAPE) {
           throw new RuntimeException("Wrong Orientation");
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g.setColor(Color.black);
        g.drawString("LANDSCAPE", 100, 300);
        double iw = pf.getImageableWidth();
        double ih = pf.getImageableHeight();
        g2d.draw(new Ellipse2D.Double(0, 0, iw, ih));
        g2d.drawString("(0,0)", 5,15);
        g2d.drawLine(0,0,300,0);
        g2d.drawString("X", 300,15);
        g2d.drawLine(0,0,0,300);
        g2d.drawString("Y",5,300);
        return PAGE_EXISTS;
    }
   private static boolean theTestPassed = false;
   private static boolean testGeneratedInterrupt = false;
   private static String failureMessage = "";
   private static Thread mainThread = null;
   private static int sleepTime = 300000;
   public static void main( String args[] ) throws Exception
    {
      mainThread = Thread.currentThread();
      try
       {
         init();
       }
      catch( TestPassedException e )
       {
         return;
       }
      try
       {
         Thread.sleep( sleepTime );
         throw new RuntimeException( "Timed out after " + sleepTime/1000 + " seconds" );
       }
      catch (InterruptedException e)
       {
         if( ! testGeneratedInterrupt ) throw e;
         testGeneratedInterrupt = false;
         if ( theTestPassed == false )
          {
            throw new RuntimeException( failureMessage );
          }
       }
    }
   public static synchronized void setTimeoutTo( int seconds )
    {
      sleepTime = seconds * 1000;
    }
   public static synchronized void pass()
    {
      Sysout.println( "The test passed." );
      Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
      if ( mainThread == Thread.currentThread() )
       {
         theTestPassed = true;
         throw new TestPassedException();
       }
      theTestPassed = true;
      testGeneratedInterrupt = true;
      mainThread.interrupt();
    }
   public static synchronized void fail()
    {
      fail( "it just plain failed! :-)" );
    }
   public static synchronized void fail( String whyFailed )
    {
      Sysout.println( "The test failed: " + whyFailed );
      Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
      if ( mainThread == Thread.currentThread() )
       {
         throw new RuntimeException( whyFailed );
       }
      theTestPassed = false;
      testGeneratedInterrupt = true;
      failureMessage = whyFailed;
      mainThread.interrupt();
    }
 }
class TestPassedException extends RuntimeException
 {
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
class TestDialog extends Dialog implements ActionListener
 {
   TextArea instructionsText;
   TextArea messageText;
   int maxStringLength = 80;
   Panel  buttonP = new Panel();
   Button passB = new Button( "pass" );
   Button failB = new Button( "fail" );
   public TestDialog( Frame frame, String name )
    {
      super( frame, name );
      int scrollBoth = TextArea.SCROLLBARS_BOTH;
      instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
      add( "North", instructionsText );
      messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
      add("Center", messageText);
      passB = new Button( "pass" );
      passB.setActionCommand( "pass" );
      passB.addActionListener( this );
      buttonP.add( "East", passB );
      failB = new Button( "fail" );
      failB.setActionCommand( "fail" );
      failB.addActionListener( this );
      buttonP.add( "West", failB );
      add( "South", buttonP );
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
   public void actionPerformed( ActionEvent e )
    {
      if( e.getActionCommand() == "pass" )
       {
         DialogOrient.pass();
       }
      else
       {
         DialogOrient.fail();
       }
    }
 }

public class DialogMargins extends Frame {
  public DialogMargins() {
     super("Dialog Margins Test");
    Button printButton = new Button ("Print ...");
    add("Center", printButton);
    printButton.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                     new MarginsPrinter();
                }
    });
    addWindowListener (new WindowAdapter() {
         public void windowClosing (WindowEvent e) {
            dispose();
         }
     });
     pack();
     setVisible (true);
  }
class MarginsPrinter implements Printable {
  PrinterJob myPrinterJob;
  PageFormat myPageFormat;
  public MarginsPrinter() {
      PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      myPrinterJob = PrinterJob.getPrinterJob();
      myPageFormat = myPrinterJob.pageDialog(aset);
      myPrinterJob.setPrintable(this, myPageFormat);
      if (myPrinterJob.printDialog(aset)) {
          try {
              myPrinterJob.print(aset);
          } catch (PrinterException pe ) {
              System.out.println("DialogMargins Exception caught:" + pe);
          }
      }
   }
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
     if (pageIndex > 0) {
        return Printable.NO_SUCH_PAGE;
     }
     Graphics2D g2d = (Graphics2D)graphics;
     g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
     g2d.drawString("ORIGIN("+pageFormat.getImageableX()+","+
                             pageFormat.getImageableY()+")", 20, 20);
     g2d.drawString("X THIS WAY", 200, 50);
     g2d.drawString("Y THIS WAY", 60 , 200);
     g2d.drawString("Graphics is " + g2d.getClass().getName(), 100, 100);
     g2d.drawRect(0,0,(int)pageFormat.getImageableWidth(),
                      (int)pageFormat.getImageableHeight());
     g2d.setColor(Color.black);
     g2d.drawRect(1,1,(int)pageFormat.getImageableWidth()-2,
                      (int)pageFormat.getImageableHeight()-2);
     return  Printable.PAGE_EXISTS;
  }
}
  public static void main( String[] args) {
  String[] instructions =
        {
         "You must have a printer available to perform this test",
         "Specify various pageformats and compare the printed results with the",
         "request."
       };
      Sysout.createDialog( );
      Sysout.printInstructions( instructions );
     new DialogMargins();
  }
}
class Sysout {
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
class TestDialog extends Dialog {
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

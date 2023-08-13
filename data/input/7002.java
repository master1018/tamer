public class Collate2DPrintingTest
    extends Frame implements Doc, Printable, ActionListener {
        Button print2D = new Button("2D Print");
        Button printMerlin = new Button("PrintService");
        PrinterJob pj = PrinterJob.getPrinterJob();
        PrintService defService = null;
        HashPrintRequestAttributeSet prSet = new HashPrintRequestAttributeSet();
    public Collate2DPrintingTest() {
        Panel butPanel = new Panel();
        butPanel.add(print2D);
        butPanel.add(printMerlin);
        print2D.addActionListener(this);
        printMerlin.addActionListener(this);
        addWindowListener (new WindowAdapter() {
            public void windowClosing (WindowEvent e) {
                dispose();
            }
        });
        add("South", butPanel);
        defService = PrintServiceLookup.lookupDefaultPrintService();
        PrintService[] pservice;
        if (defService == null) {
            pservice = PrintServiceLookup.lookupPrintServices(null, null);
            if (pservice.length == 0) {
                throw new RuntimeException("No printer found.  TEST ABORTED");
            }
            defService = pservice[0];
        }
        prSet.add(SheetCollate.COLLATED);
        prSet.add(new Copies(2));
        pj.setPrintable(Collate2DPrintingTest.this);
        setSize(300, 200);
        setVisible(true);
    }
    public int print(Graphics g, PageFormat pf, int pageIndex)
          throws PrinterException {
        g.drawString("Page: " + pageIndex, 100, 100);
        if (pageIndex == 2) {
            return Printable.NO_SUCH_PAGE;
        } else {
            return Printable.PAGE_EXISTS;
        }
    }
    public void actionPerformed (ActionEvent ae) {
        try {
            if (ae.getSource() == print2D) {
                if (pj.printDialog(prSet)) {
                    pj.print(prSet);
                }
            } else {
                DocPrintJob pj = defService.createPrintJob();
                pj.print(this, prSet);
            }
            System.out.println ("DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public DocAttributeSet getAttributes() {
        return null;
    }
    public DocFlavor getDocFlavor() {
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        return flavor;
    }
    public Object getPrintData() {
        return this;
    }
    public Reader getReaderForText() {
        return null;
    }
    public InputStream getStreamForBytes() {
        return null;
    }
  public static void main( String[] args) {
  String[] instructions =
        {
         "You must have a printer available to perform this test",
         "The print result should be collated."
       };
      Sysout.createDialog( );
      Sysout.printInstructions( instructions );
     new Collate2DPrintingTest();
  }
}
class Sysout {
   private static TestDialog dialog;
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
    }
 }

public class ConstrainedPrintingTest implements ActionListener
 {
    final Frame frame = new Frame("PrintTest");
    final Button button = new Button("Print");
    final Panel panel = new Panel();
    final Component testComponent = new Component() {
        public void paint(Graphics g) {
            ConstrainedPrintingTest.paintOutsideBounds(this, g, Color.green);
        }
        public Dimension getPreferredSize() {
            return new Dimension(100, 100);
        }
    };
    final Canvas testCanvas = new Canvas() {
        public void paint(Graphics g) {
            ConstrainedPrintingTest.paintOutsideBounds(this, g, Color.red);
            Dimension panelSize = panel.getSize();
            Rectangle b = getBounds();
            g.setColor(Color.red);
            g.setClip(null);
            for (int i = panelSize.height - b.y; i < b.height; i+= 10) {
                g.drawLine(0, i, b.width, i);
            }
        }
        public Dimension getPreferredSize() {
            return new Dimension(100, 100);
        }
    };
   public void init()
    {
        button.addActionListener(this);
        panel.setBackground(Color.white);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.add(testComponent);
        panel.add(testCanvas);
        frame.setLayout(new BorderLayout());
        frame.add(button, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.setSize(200, 250);
        frame.validate();
        frame.setResizable(false);
      String[] instructions =
       {
         "1.Look at the frame titled \"PrintTest\". If you see green or",
         "  red lines on the white area below the \"Print\" button, the",
         "  test fails. Otherwise go to step 2.",
         "2.Press \"Print\" button. The print dialog will appear. Select",
         "  a printer and proceed. Look at the output. If you see multiple",
         "  lines outside of the frame bounds or in the white area below",
         "  the image of the \"Print\" button, the test fails. Otherwise",
         "  the test passes."
       };
      Sysout.createDialogWithInstructions( instructions );
    }
   public void start ()
    {
        frame.setVisible(true);
    }
    public void stop() {
        frame.setVisible(false);
    }
    public void destroy() {
        frame.dispose();
    }
    public void actionPerformed(ActionEvent e) {
        PageAttributes pa = new PageAttributes();
        pa.setPrinterResolution(36);
        PrintJob pjob = frame.getToolkit().getPrintJob(frame, "NewTest",
                                                       new JobAttributes(),
                                                       pa);
        if (pjob != null) {
            Graphics pg = pjob.getGraphics();
            if (pg != null) {
                pg.translate(20, 20);
                frame.printAll(pg);
                pg.dispose();
            }
            pjob.end();
        }
    }
    public static void paintOutsideBounds(Component comp,
                                          Graphics g,
                                          Color color) {
        Dimension dim = comp.getSize();
        g.setColor(color);
        g.setClip(0, 0, dim.width * 2, dim.height * 2);
        for (int i = 0; i < dim.height * 2; i += 10) {
            g.drawLine(dim.width, i, dim.width * 2, i);
        }
        g.setClip(null);
        for (int i = 0; i < dim.width * 2; i += 10) {
            g.drawLine(i, dim.height, i, dim.height * 2);
        }
        g.setClip(new Rectangle(0, 0, dim.width * 2, dim.height * 2));
        for (int i = 0; i < dim.width; i += 10) {
            g.drawLine(dim.width * 2 - i, 0, dim.width * 2, i);
        }
    }
    public static void main(String[] args) {
        ConstrainedPrintingTest c = new ConstrainedPrintingTest();
        c.init();
        c.start();
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

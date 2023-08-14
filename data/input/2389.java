public class PrintAWTImage extends Frame
                           implements ActionListener, Printable {
 public Image imgJava;
 public static void main(String args[]) {
    PrintAWTImage f = new PrintAWTImage();
    f.show();
 }
 public PrintAWTImage() {
    Button printButton = new Button("Print");
    setLayout(new FlowLayout());
    add(printButton);
    printButton.addActionListener(this);
    addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
             System.exit(0);
            }
    });
    pack();
 }
 public void actionPerformed(ActionEvent e) {
   PrinterJob pj = PrinterJob.getPrinterJob();
   if (pj != null && pj.printDialog()) {
       pj.setPrintable(this);
       try {
            pj.print();
      } catch (PrinterException pe) {
      } finally {
         System.err.println("PRINT RETURNED");
      }
   }
 }
    public int print(Graphics g, PageFormat pgFmt, int pgIndex) {
      if (pgIndex > 0)
         return Printable.NO_SUCH_PAGE;
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(pgFmt.getImageableX(), pgFmt.getImageableY());
      Image imgJava = Toolkit.getDefaultToolkit().getImage("duke.gif");
      g2d.drawImage(imgJava, 0, 0, this);
      return Printable.PAGE_EXISTS;
    }
}

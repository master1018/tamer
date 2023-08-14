public class PrintARGBImage implements Printable {
    static String[] text = {
     "This is a manual test which needs a printer installed",
     "If you have no printer installed you CANNOT use this test",
     "It runs automatically and sends one page to the default printer",
     "It passes if the text shows through the rectangular image",
    };
    public static void main( String[] args ) {
        for (int i=0;i<text.length;i++) {
            System.out.println(text[i]);
        }
        try {
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setPrintable(new PrintARGBImage());
            pj.print();
            } catch (Exception ex) {
        }
    }
    public int print(Graphics g, PageFormat pf, int pageIndex)
               throws PrinterException{
        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        g2.setColor( Color.BLACK );
        g2.drawString("This text should be visible through the image", 0, 20);
        BufferedImage bi = new BufferedImage(100, 100,
                                              BufferedImage.TYPE_INT_ARGB );
        Graphics ig = bi.createGraphics();
        ig.setColor( new Color( 192, 192, 192, 80 ) );
        ig.fillRect( 0, 0, 100, 100 );
        ig.setColor( Color.BLACK );
        ig.drawRect( 0, 0, 99, 99 );
        ig.dispose();
        g2.drawImage(bi, 10, 0, 90, 90, null );
        return PAGE_EXISTS;
    }
}

public class Margins implements Printable {
    public static void main(String args[]) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        Paper paper = pageFormat.getPaper();
        double wid = paper.getWidth();
        double hgt = paper.getHeight();
        paper.setImageableArea(0, -10, wid, hgt);
        pageFormat = job.pageDialog(pageFormat);
        pageFormat.setPaper(paper);
        job.setPrintable(new Margins(), pageFormat);
        try {
            job.print();
        } catch (PrinterException e) {
        }
        paper.setImageableArea(0, 0, wid, hgt+72);
        pageFormat = job.pageDialog(pageFormat);
        pageFormat.setPaper(paper);
        job.setPrintable(new Margins(), pageFormat);
        try {
           job.print();
        } catch (PrinterException e) {
        }
   }
   public int print(Graphics g, PageFormat pf, int page)
       throws PrinterException {
       if (page > 0) {
           return NO_SUCH_PAGE;
       }
       int ix = (int)pf.getImageableX();
       int iy = (int)pf.getImageableY();
       int iw = (int)pf.getImageableWidth();
       int ih = (int)pf.getImageableHeight();
       System.out.println("ix="+ix+" iy="+iy+" iw="+iw+" ih="+ih);
       if ((ix < 0) || (iy < 0)) {
           throw new RuntimeException("Imageable x or y is a negative value.");
       }
       Paper paper = pf.getPaper();
       double wid = paper.getWidth();
       double hgt = paper.getHeight();
       if ((ix+iw > wid) || (iy+ih > hgt)) {
           throw new RuntimeException("Printable width or height exceeds paper width or height.");
       }
       Graphics2D g2d = (Graphics2D)g;
       g2d.translate(ix, iy);
       g2d.setColor(Color.black);
       g2d.drawRect(1, 1, iw-2, ih-2);
       return PAGE_EXISTS;
   }
}

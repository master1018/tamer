public class PrintSE implements Printable {
    public static void main(String[] args) throws Exception {
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        if (service == null) {
            System.out.println("No print service found.");
            return;
        }
        SimpleDoc doc =
             new SimpleDoc(new PrintSE(),
                           DocFlavor.SERVICE_FORMATTED.PRINTABLE,
                           new HashDocAttributeSet());
        DocPrintJob job = service.createPrintJob();
        job.print(doc, new HashPrintRequestAttributeSet());
    }
    public int print(Graphics g, PageFormat pf, int pg) {
       if (pg > 0) return NO_SUCH_PAGE;
       g.drawString("Test passes.", 100, 100);
       return PAGE_EXISTS;
   }
}

public class SameService implements Printable {
    public static void main(String args[]) throws Exception {
        PrinterJob job1 = PrinterJob.getPrinterJob();
        job1.setPrintable(new SameService());
        PrintService service1 = job1.getPrintService();
        PrinterJob job2 = PrinterJob.getPrinterJob();
        job2.setPrintable(new SameService());
        PrintService service2 = job2.getPrintService();
        if (service1 != service2) {
           throw new RuntimeException("Duplicate service created");
        }
    }
     public int print(Graphics g, PageFormat pf, int pi)
                       throws PrinterException  {
          return NO_SUCH_PAGE;
     }
}

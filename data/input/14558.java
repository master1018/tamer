public class PageFormatChange {
    static String[] text = {
    "This is is a manual test intended to be run on Windows, and you",
    "must have at least two printers installed, and ideally the second",
    "printer should support large paper sizes. When the pageDialog appears",
    "first change printers, then choose a large paper size, then OK",
    "the dialog. The test will throw an Exception if it fails",
    };
    public static void main(String[] args) {
        if (!System.getProperty("os.name","").startsWith("Windows")) {
           System.out.println("Not Windows, so test is not applicable");
           return;
        }
        for (String s : text) {
            System.out.println(s);
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        PrintService service1 = job.getPrintService();
        PageFormat pf1 = job.defaultPage();
        PageFormat pf2 = job.pageDialog(pf1);
        PrintService service2 = job.getPrintService();
        if (service1.equals(service2)) {
           System.err.println("You must select a different printer!");
           System.err.println("Test cannot continue");
        }
        if (pf1.equals(pf2)) {
           System.err.println("You must select a different paper size!");
        }
        int pw = (int)(pf2.getWidth()+0.5);
        int ph = (int)(pf2.getHeight()+0.5);
        int iw = (int)(pf2.getImageableWidth()+0.5);
        int ih = (int)(pf2.getImageableHeight()+0.5);
        int ix = (int)(pf2.getImageableX()+0.5);
        int iy = (int)(pf2.getImageableY()+0.5);
        int expectedWidth = ix*2+iw;
        int expectedHeight = iy*2+ih;
        if (expectedWidth != pw || expectedHeight != ph) {
            throw new RuntimeException("Unexpected size");
        }
        displayPageFormat(pf2);
    }
    public static void displayPageFormat(PageFormat pf)
    {
        System.out.println("------- Page Format -------");
        System.out.println("ImageableX = " + pf.getImageableX());
        System.out.println("ImageableY = " + pf.getImageableY());
        System.out.println("ImageableWidth = " + pf.getImageableWidth());
        System.out.println("ImageableHeight = " + pf.getImageableHeight());
        System.out.println("Width = " + pf.getWidth());
        System.out.println("Height = " + pf.getHeight());
    }
}

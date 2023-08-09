public class Applet4PrintServiceLookup extends PrintServiceLookup {
    PrintService defaultPrintService = null;
    public synchronized PrintService[] getPrintServices() {
        PrintService []printServices = new PrintService[1];
        printServices[0] = getDefaultPrintService();
        return printServices;
    }
    public PrintService[] getPrintServices(DocFlavor flavor,
                                           AttributeSet attributes) {
        return getPrintServices();
    }
    public MultiDocPrintService[]
        getMultiDocPrintServices(DocFlavor[] flavors,
                                 AttributeSet attributes) {
        return new MultiDocPrintService[0];
    }
    public synchronized PrintService getDefaultPrintService() {
        if (defaultPrintService == null) {
           defaultPrintService = new Applet4PrintService();
        }
        return defaultPrintService;
    }
}

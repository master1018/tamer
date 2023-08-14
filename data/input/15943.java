public class LookupServices {
    public static void main (String [] args) {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.GIF;
        HashPrintRequestAttributeSet prSet = null;
        PrintService[] serv = PrintServiceLookup.lookupPrintServices(flavor, null);
        System.out.println("default "+PrintServiceLookup.lookupDefaultPrintService());
        if (serv.length==0) {
            System.out.println("no PrintService supports GIF");
            return;
        }
        System.out.println("Selected print service: "+ serv[0]);
    }
}

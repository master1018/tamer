public class GetCopiesSupported {
    public static void main(String args[]) {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        PrintService[] pservice;
        if (service == null) {
             pservice = PrintServiceLookup.lookupPrintServices(null, null);
            if (pservice.length == 0) {
                    throw new RuntimeException("No printer found.  TEST ABORTED");
            }
            service = pservice[0];
        }
        if (service != null) {
            CopiesSupported c = (CopiesSupported)
               service.getSupportedAttributeValues(Copies.class,
                                                   null, null);
           System.out.println("CopiesSupported : "+c);
        }
    }
}

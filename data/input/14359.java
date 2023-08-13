public class CheckDupFlavor {
    public static void main(String[] args){
        PrintService defService = PrintServiceLookup.lookupDefaultPrintService();
        PrintService[] pservice;
        if (defService == null) {
            pservice = PrintServiceLookup.lookupPrintServices(null, null);
            if (pservice.length == 0) {
                throw new RuntimeException("No printer found.  TEST ABORTED");
            }
            defService = pservice[0];
        }
        System.out.println("PrintService = "+defService);
        DocFlavor[] flavors = defService.getSupportedDocFlavors();
        if (flavors==null) {
            System.out.println("No flavors supported. Test PASSED.");
            return;
        }
        ArrayList flavorList = new ArrayList();
        for (int i=0; i<flavors.length; i++) {
            if (flavors[i] == null) {
                 throw new RuntimeException("Null flavor. Test FAILED.");
            } else if (flavorList.contains(flavors[i])) {
                 throw new RuntimeException("\n\tDuplicate flavor found : "+flavors[i]+" : Test FAILED.");
            } else {
                flavorList.add(flavors[i]);
            }
        }
        System.out.println("No duplicate found. Test PASSED.");
    }
}

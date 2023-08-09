public class SidesPageRangesTest {
         public SidesPageRangesTest() {
                super();
        }
        public static void main(java.lang.String[] args) {
                SidesPageRangesTest pd = new SidesPageRangesTest();
                PrintService defService = null;
                DocFlavor flavors[]  = null;
                PrintService[] pservice;
                defService = PrintServiceLookup.lookupDefaultPrintService();
                if (defService == null) {
                    pservice = PrintServiceLookup.lookupPrintServices(null, null);
                    if (pservice.length == 0) {
                        throw new RuntimeException("Printer is required for this test.  TEST ABORTED");
                    }
                    defService = pservice[0];
                }
                System.out.println("Default Print Service "+defService);
                if (defService.isAttributeCategorySupported(PageRanges.class)) {
                        System.out.println("\nPageRanges Attribute category is supported");
                } else {
                        System.out.println("\nPageRanges Attribute category is not supported. terminating...");
                        return;
                }
                flavors = defService.getSupportedDocFlavors();
                System.out.println("\nGetting Supported values for PageRanges for each supported DocFlavor");
                System.out.println("===============================================================\n");
                for (int y = 0; y < flavors.length; y ++) {
                    System.out.println("\n\n");
                    System.out.println("Doc Flavor: "+flavors[y]);
                    System.out.println("-----------------------------");
                    Object vals = defService.getSupportedAttributeValues(PageRanges.class, flavors[y], null);
                    if (vals == null) {
                        System.out.println("No supported values for PageRanges for this doc flavor. ");
                    }
                    PageRanges[] pr = null;
                    if (vals instanceof PageRanges[]) {
                        pr = (PageRanges[]) vals;
                        for (int x = 0; x < pr.length; x ++) {
                            System.out.println("\nSupported Value "+pr[x]);
                            System.out.println("is "+pr[x]+" value supported? "+defService.isAttributeValueSupported(pr[x], flavors[y], null));
                            if (!defService.isAttributeValueSupported(pr[x], flavors[y], null)) {
                                throw new RuntimeException("PageRanges contradicts getSupportedAttributeValues");
                            }
                        }
                    } else if (vals instanceof PageRanges) {
                        System.out.println(vals);
                        System.out.println("is "+vals+" value supported? "+defService.isAttributeValueSupported((javax.print.attribute.Attribute)vals, flavors[y], null));
                        if (!defService.isAttributeValueSupported((javax.print.attribute.Attribute)vals, flavors[y], null)) {
                            throw new RuntimeException("PageRanges contradicts getSupportedAttributeValues");
                        }
                    }
                    vals = defService.getSupportedAttributeValues(Sides.class, flavors[y], null);
                    if (vals == null) {
                        System.out.println("No supported values for Sides for this doc flavor. ");
                    }
                    Sides[] s = null;
                    if (vals instanceof Sides[]) {
                        s = (Sides[]) vals;
                        for (int x = 0; x < s.length; x ++) {
                            System.out.println("\nSupported Value "+s[x]);
                            System.out.println("is "+s[x]+" value supported? "+defService.isAttributeValueSupported(s[x], flavors[y], null));
                            if  (!defService.isAttributeValueSupported(s[x], flavors[y], null)) {
                                throw new RuntimeException("Sides contradicts getSupportedAttributeValues");
                            }
                        }
                    }
                }
        }
}

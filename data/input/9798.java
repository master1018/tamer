public class AttributeTest {
        public AttributeTest() {
                PrintService service[] = PrintServiceLookup.lookupPrintServices(null, null);
                if (service.length == 0) {
                        throw new RuntimeException("No printer found.  TEST ABORTED");
                }
                for (int x = 0; x < service.length; x ++) {
                        DocFlavor flavors[] = service[x].getSupportedDocFlavors();
                        for (int y = 0; y < flavors.length; y ++) {
                                Object attrVal = service[x].getSupportedAttributeValues(Media.class, flavors[y], null);
                                if (attrVal == null) {
                                        continue;
                                }
                                Media attr[] = (Media[]) attrVal;
                                for (int z = 0; z < attr.length; z ++) {
                                        if (!service[x].isAttributeValueSupported(attr[z], flavors[y], null)) {
                                                throw new RuntimeException("ERROR: There is a conflict between getSupportedAttrValues " +
                                                " and isAttributeValueSupported, for the attribute: " + attr[z] +
                                                ", where the flavor is: " + flavors[y] + " and the print service is: " +
                                                service[x] + "\n");
                                        }
                                }
                        }
                }
                System.out.println("Test Passed");
        }
        public static void main (String args[]) {
                AttributeTest test = new AttributeTest();
        }
}

public class CollateAttr {
   public static void main(String args[]) throws Exception {
      PrintService[] services =
            PrintServiceLookup.lookupPrintServices(null,null);
      for (int i=0; i<services.length; i++) {
          if (services[i].isAttributeCategorySupported(SheetCollate.class)) {
              System.out.println("Testing " + services[i]);
              services[i].isAttributeValueSupported(SheetCollate.COLLATED,
                                                    null, null);
              services[i].getSupportedAttributeValues(SheetCollate.class,
                                                      null,null);
          }
      }
   }
}

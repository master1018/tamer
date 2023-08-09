public class SupportedPrintableAreas {
  public static void main(String[] args) {
     PrintService[] svc;
     PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
     if (printer == null) {
         svc = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
         if (svc.length == 0) {
             throw new RuntimeException("Printer is required for this test.  TEST ABORTED");
         }
         printer = svc[0];
     }
     System.out.println("PrintService found : "+printer);
     if (!printer.isAttributeCategorySupported(MediaPrintableArea.class)) {
         return;
     }
     Object value = printer.getSupportedAttributeValues(
                    MediaPrintableArea.class, null, null);
     if (!value.getClass().isArray()) {
         throw new RuntimeException("unexpected value");
      }
     PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
     value = printer.getSupportedAttributeValues(
                    MediaPrintableArea.class, null, aset);
     if (!value.getClass().isArray()) {
         throw new RuntimeException("unexpected value");
      }
     Media media = (Media)printer.getDefaultAttributeValue(Media.class);
     aset.add(media);
     value = printer.getSupportedAttributeValues(
                    MediaPrintableArea.class, null, aset);
     if (!value.getClass().isArray()) {
         throw new RuntimeException("unexpected value");
     }
     aset.add(MediaTray.MANUAL);
     value = printer.getSupportedAttributeValues(
                    MediaPrintableArea.class, null, aset);
     if ((value != null) && !value.getClass().isArray()) {
         throw new RuntimeException("unexpected value");
     }
  }
}

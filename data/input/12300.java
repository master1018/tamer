public class Chroma {
   public static void main(String args[]) {
      StreamPrintServiceFactory []fact =
        StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
              DocFlavor.SERVICE_FORMATTED.PRINTABLE,
              DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
      if (fact.length != 0) {
          OutputStream out = new ByteArrayOutputStream();
          StreamPrintService sps = fact[0].getPrintService(out);
          checkChroma(sps);
      }
      PrintService defSvc = PrintServiceLookup.lookupDefaultPrintService();
      if (defSvc != null) {
           checkChroma(defSvc);
      }
   }
    static void checkChroma(PrintService svc) {
       if (svc.isAttributeCategorySupported(Chromaticity.class)) {
            svc.getSupportedAttributeValues(Chromaticity.class,null,null);
       }
    }
}

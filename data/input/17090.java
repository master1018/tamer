public class PSCopiesFlavorTest {
   public static void main(String args[]) {
       DocFlavor flavor = DocFlavor.INPUT_STREAM.POSTSCRIPT;
       PrintService[] ps = PrintServiceLookup.lookupPrintServices(flavor, null);
       if (ps.length > 0) {
           System.out.println("found PrintService: "+ps[0]);
           Copies c = new Copies(1);
           PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
           aset.add(c);
           boolean suppVal = ps[0].isAttributeValueSupported(c, flavor, null);
           AttributeSet us = ps[0].getUnsupportedAttributes(flavor, aset);
           if (suppVal || us == null) {
               throw new RuntimeException("Copies should be unsupported value");
           }
           Object value = ps[0].getSupportedAttributeValues(Copies.class,
                                                            flavor, null);
            if(value instanceof CopiesSupported) {
                throw new RuntimeException("Copies should have no supported values.");
            }
       }
   }
}

public class ChromaticityValues {
    public static void main(String args[]) {
        System.out.println("=======================================================================");
        System.out.println("INSTRUCTIONS: This test is only for WINDOWS platform. ");
        System.out.println("You should have a printer configured as your default printer in your system.");
        System.out.println("=======================================================================");
        String os=System.getProperty("os.name").toLowerCase();
        if (!(os.indexOf("win")>=0)) {
            System.out.println("Not a Windows System.  TEST ABORTED");
            return;
        }
        PrintService pservice = PrintServiceLookup.lookupDefaultPrintService();
        if (pservice == null) {
            throw new RuntimeException("A printer is required for this test.");
        }
        System.out.println("Default Service is "+pservice);
        ColorSupported psa = (ColorSupported)pservice.getAttribute(ColorSupported.class);
        ArrayList cValues = new ArrayList();
        if (pservice.isAttributeCategorySupported(Chromaticity.class)) {
            Chromaticity[] values =(Chromaticity[])
                (pservice.getSupportedAttributeValues(Chromaticity.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null));
            if ((values != null) && (values.length > 0)) {
                for (int i=0; i<values.length; i++) {
                    cValues.add(values[i]);
                }
            } else {
                System.out.println("Chromaticity value is unknown. TEST ABORTED");
                return;
            }
        } else {
            System.out.println("Chromaticity is not supported. TEST ABORTED");
            return;
        }
        if (psa != null) {
            if (psa.equals(ColorSupported.SUPPORTED)) {
                if (cValues.size() < 2) {
                    throw new RuntimeException("ColorSupported is supported, values for Chromaticity should be monochrome and color.");
                }
            } else {
                if ((cValues.size() != 1) ||
                    (!cValues.contains(Chromaticity.MONOCHROME))) {
                    throw new RuntimeException("ColorSupported is not supported, values for Chromaticity should only be monochrome.");
                }
            }
        } else { 
            if (!cValues.contains(Chromaticity.COLOR)) {
                throw new RuntimeException("ColorSupported is unknown, values for Chromaticity should at least include color.");
            }
        }
    }
}

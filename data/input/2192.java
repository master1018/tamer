public class GetMediasTest {
    public static void main(String[] args) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for(final PrintService service: services) {
            Thread thread = new Thread() {
                public void run() {
                    service.getSupportedAttributeValues(Media.class, null, null);
                }
            };
            thread.start();
        }
    }
}

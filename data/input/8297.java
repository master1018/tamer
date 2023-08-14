public class DocumentRootTest {
    public static int test(URL documentBase, URL codeBase) {
        int error = 0;
        MLetContent mc = new MLetContent(
                documentBase,
                new HashMap<String,String>(),
                new ArrayList<String>(),
                new ArrayList<String>());
        System.out.println("\nACTUAL   DOCUMENT BASE = " + mc.getDocumentBase());
        System.out.println("EXPECTED DOCUMENT BASE = " + documentBase);
        if (!documentBase.equals(mc.getDocumentBase())) {
            System.out.println("ERROR: Wrong document base");
            error++;
        };
        System.out.println("ACTUAL   CODEBASE = " + mc.getCodeBase());
        System.out.println("EXPECTED CODEBASE = " + codeBase);
        if (!codeBase.equals(mc.getCodeBase())) {
            System.out.println("ERROR: Wrong code base");
            error++;
        };
        return error;
    }
    public static void main(String[] args) throws Exception {
        int error = 0;
        error += test(new URL("file:/mlet.txt"), new URL("file:/"));
        error += test(new URL("http:
        if (error > 0) {
            System.out.println("\nTest FAILED!\n");
            throw new IllegalArgumentException("Test FAILED!");
        } else {
            System.out.println("\nTest PASSED!\n");
        }
    }
}

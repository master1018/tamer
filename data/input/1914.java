public class EncodingNothing {
    public static void main(String[] args) throws Throwable {
        int failed = 0;
        for (Charset cs : Charset.availableCharsets().values()) {
            if (! cs.canEncode())
                continue;
            System.out.printf("%s: ", cs.name());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, cs);
            osw.close();
            if (baos.size() != 0) {
                System.out.printf(" Failed:  output bytes=%d", baos.size());
                failed++;
            }
            System.out.println();
        }
        if (failed != 0)
            throw new AssertionError("Some tests failed");
    }
}

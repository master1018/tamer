public class EnumAfterClose {
    public static void main(String args[]) throws Exception {
        Enumeration e;
        try (ZipFile zf = new ZipFile(new File(System.getProperty("test.src", "."),
                                               "input.zip"))) {
            e = zf.entries();
        }
        try {
            if (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry)e.nextElement();
            }
        } catch (IllegalStateException ie) {
        }
    }
}

public class GetMethodsReturnClones {
    private final static String BASE = System.getProperty("test.src", ".") +
        System.getProperty("file.separator");
    public static void main(String[] args) throws Exception {
        List<JarEntry> entries = new ArrayList<>();
        try (JarFile jf = new JarFile(BASE + "test.jar", true)) {
            byte[] buffer = new byte[8192];
            Enumeration<JarEntry> e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                entries.add(je);
                try (InputStream is = jf.getInputStream(je)) {
                    while (is.read(buffer, 0, buffer.length) != -1) {
                    }
                }
            }
        }
        for (JarEntry je : entries) {
            Certificate[] certs = je.getCertificates();
            CodeSigner[] signers = je.getCodeSigners();
            if (certs != null) {
                certs[0] = null;
                certs = je.getCertificates();
                if (certs[0] == null) {
                    throw new Exception("Modified internal certs array");
                }
            }
            if (signers != null) {
                signers[0] = null;
                signers = je.getCodeSigners();
                if (signers[0] == null) {
                    throw new Exception("Modified internal codesigners array");
                }
            }
        }
    }
}

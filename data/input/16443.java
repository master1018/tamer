public class MevNPE {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."), "Signed.jar");
        try (JarFile jf = new JarFile(f, true)) {
            try (InputStream s1 = jf.getInputStream(
                    jf.getJarEntry(JarFile.MANIFEST_NAME))) {
                s1.read(new byte[10000]);
            };
            try (InputStream s2 = jf.getInputStream(
                    jf.getJarEntry(JarFile.MANIFEST_NAME))) {
                s2.read(new byte[10000]);
            };
        }
    }
}

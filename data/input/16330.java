public class ScanSignedJar {
    public static void main(String[] args) throws Exception {
        boolean isSigned = false;
        try (JarFile file = new JarFile(new File(System.getProperty("test.src","."),
                 "bogus-signerinfo-attr.jar"))) {
            byte[] buffer = new byte[8192];
            for (Enumeration entries = file.entries(); entries.hasMoreElements();) {
                JarEntry entry = (JarEntry) entries.nextElement();
                try (InputStream jis = file.getInputStream(entry)) {
                    while (jis.read(buffer, 0, buffer.length) != -1) {
                    }
                }
                if (entry.getCertificates() != null) {
                    isSigned = true;
                }
                System.out.println((isSigned ? "[signed] " : "\t ") +
                    entry.getName());
            }
        }
        if (isSigned) {
            System.out.println("\nJAR file has signed entries");
        } else {
            throw new Exception("Failed to detect that the JAR file is signed");
        }
    }
}

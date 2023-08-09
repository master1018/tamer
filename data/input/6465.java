public class JarSigningNonAscii {
    private static String jarFile;
    private static String keystore;
    public static void main(String[] args) throws Exception {
        String srcDir = System.getProperty("test.src", ".");
        String destDir = System.getProperty("test.classes", ".");
        String unsignedJar = srcDir + "/JarSigning_RU.jar";
        String signedJar = destDir + "/JarSigning_RU.signed.jar";
        String keystore = srcDir + "/JarSigning.keystore";
        try {
            File removeMe = new File(signedJar);
            removeMe.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] jsArgs = {
                        "-keystore", keystore,
                        "-storepass", "bbbbbb",
                        "-signedJar", signedJar,
                        unsignedJar, "b"
                        };
        JarSigner.main(jsArgs);
        JarEntry je;
        JarFile jf = new JarFile(signedJar, true);
        Vector entriesVec = new Vector();
        byte[] buffer = new byte[8192];
        Enumeration entries = jf.entries();
        while (entries.hasMoreElements()) {
            je = (JarEntry)entries.nextElement();
            entriesVec.addElement(je);
            InputStream is = jf.getInputStream(je);
            int n;
            while ((n = is.read(buffer, 0, buffer.length)) != -1) {
            }
            is.close();
        }
        jf.close();
        Manifest man = jf.getManifest();
        int isSignedCount = 0;
        if (man != null) {
            Enumeration e = entriesVec.elements();
            while (e.hasMoreElements()) {
                je = (JarEntry) e.nextElement();
                String name = je.getName();
                Certificate[] certs = je.getCertificates();
                if ((certs != null) && (certs.length > 0)) {
                    isSignedCount++;
                }
            }
        }
        if (isSignedCount != 4) {
            throw new SecurityException("error signing JAR file");
        }
        System.out.println("jar verified");
    }
}

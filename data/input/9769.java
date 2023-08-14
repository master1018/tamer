public class LargeJarEntry {
    public static void main(String[] args) throws Exception {
        String srcDir = System.getProperty("test.src", ".");
        String keystore = srcDir + "/JarSigning.keystore";
        String jarName = "largeJarEntry.jar";
        System.setProperty("java.io.tmpdir", System.getProperty("user.dir"));
        byte[] bytes = new byte[1000000];
        CRC32 crc = new CRC32();
        for (int i=0; i<8; i++) {
            crc.update(bytes);
        }
        JarEntry je = new JarEntry("large");
        je.setSize(8000000l);
        je.setMethod(JarEntry.STORED);
        je.setCrc(crc.getValue());
        File file = new File(jarName);
        FileOutputStream os = new FileOutputStream(file);
        JarOutputStream jos = new JarOutputStream(os);
        jos.setMethod(JarEntry.STORED);
        jos.putNextEntry(je);
        for (int i=0; i<8; i++) {
            jos.write(bytes, 0, bytes.length);
        }
        jos.close();
        String[] jsArgs = { "-keystore", keystore, "-storepass", "bbbbbb",
                jarName, "b" };
        try {
            JarSigner.main(jsArgs);
        } catch (OutOfMemoryError err) {
            throw new Exception("Test failed with OutOfMemoryError", err);
        } finally {
            file.delete();
        }
    }
}

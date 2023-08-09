public class NoExtensionSignature {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."), "test.zip");
        ZipInputStream zis = new ZipInputStream (new FileInputStream(f));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null)
            while (zis.read() != -1 );
    }
}

public class ZipMeUp {
    static final CRC32 crc = new CRC32();
    private static String SOME_KLASS = ".Some";
    static byte[] getManifestAsBytes(int nchars) throws IOException {
        crc.reset();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CheckedOutputStream cos = new CheckedOutputStream(baos, crc);
        PrintStream ps = new PrintStream(cos);
        ps.println("Manifest-Version: 1.0");
        ps.print("Main-Class: ");
        for (int i = 0 ; i < nchars - SOME_KLASS.length() ; i++) {
            ps.print(i%10);
        }
        ps.println(SOME_KLASS);
        cos.flush();
        cos.close();
        ps.close();
        return baos.toByteArray();
    }
    public static void main(String...args) throws Exception  {
        FileOutputStream fos = new FileOutputStream(args[0]);
        ZipOutputStream zos = new ZipOutputStream(fos);
        byte[] manifest = getManifestAsBytes(Integer.parseInt(args[1]));
        ZipEntry ze = new ZipEntry("META-INF/MANIFEST.MF");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(manifest.length);
        ze.setCompressedSize(manifest.length);
        ze.setCrc(crc.getValue());
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.write(manifest);
        zos.flush();
        ze = new ZipEntry(SOME_KLASS + ".class");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(0);
        ze.setCompressedSize(0);
        ze.setCrc(0);
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.flush();
        zos.closeEntry();
        zos.close();
        System.exit(0);
    }
}

public class CopyJar {
    public static void main(String args[]) throws Exception {
        try (ZipFile zf = new ZipFile(new File(System.getProperty("test.src", "."),
                                               "input.jar"))) {
            ZipEntry ze = zf.getEntry("ReleaseInflater.java");
            ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
            InputStream in = zf.getInputStream(ze);
            byte[] b = new byte[128];
            int n;
            zos.putNextEntry(ze);
            while((n = in.read(b)) != -1) {
                zos.write(b, 0, n);
            }
            zos.close();
        }
    }
}

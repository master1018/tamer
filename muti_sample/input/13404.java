public class JavaUtils {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(JavaUtils.class.getName());
    private JavaUtils() {
    }
    public static byte[] getBytesFromFile(String fileName)
        throws FileNotFoundException, IOException {
        byte refBytes[] = null;
        FileInputStream fisRef = new FileInputStream(fileName);
        try {
            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            while ((len = fisRef.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            refBytes = baos.toByteArray();
        } finally {
            fisRef.close();
        }
        return refBytes;
    }
    public static void writeBytesToFilename(String filename, byte[] bytes) {
        FileOutputStream fos = null;
        try {
            if (filename != null && bytes != null) {
                File f = new File(filename);
                fos = new FileOutputStream(f);
                fos.write(bytes);
                fos.close();
            } else {
                log.log(java.util.logging.Level.FINE, "writeBytesToFilename got null byte[] pointed");
            }
        } catch (IOException ex) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {}
            }
        }
    }
    public static byte[] getBytesFromStream(InputStream inputStream)
        throws IOException {
        byte refBytes[] = null;
        UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        refBytes = baos.toByteArray();
        return refBytes;
    }
}

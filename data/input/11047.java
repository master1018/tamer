public class StreamIOAfterClose {
    private static byte[] compressed = {
        31,-117,8,0,0,0,0,0,0,0,-85,-107,-79,74,85,97,117,48,
        -9,117,-47,114,15,-87,-27,-9,-54,-48,49,-108,17,19,20,
        118,-87,-84,78,-15,-10,-87,-112,51,115,16,85,81,54,11,
        114,44,11,98,116,17,102,-10,72,-10,80,-79,101,14,47,-50,
        16,117,-9,-83,16,13,-55,83,83,103,-30,-117,-42,-82,-105,
        -119,46,-16,20,-111,-85,-16,-48,54,79,-53,-76,116,
        -80,-78,77,-88,-85,50,113,-54,15,-74,-28,-44,101,-43,47,
        85,54,-74,1,0,85,69,28,117,100,0,0,0
    };
    private static void testRead(InputStream in) throws Exception {
        in.close();
        try {
            in.read();
            throw new Exception("read allowed after stream is closed");
        } catch (IOException e) {
        }
    }
    private static void testWrite(ZipOutputStream out) throws Exception {
        out.close();
        try {
            out.putNextEntry(new ZipEntry(""));
            throw new Exception("write allowed after stream is closed");
        } catch (IOException e) {
        }
    }
    public static void main(String argv[]) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
        zos.putNextEntry(new ZipEntry("1"));
        testWrite(zos);
        JarOutputStream jos = new JarOutputStream(new ByteArrayOutputStream());
        jos.putNextEntry(new ZipEntry("1"));
        testWrite(jos);
        InputStream bis = new ByteArrayInputStream(new byte[10]);
        InputStream bis1 = new ByteArrayInputStream(compressed);
        testRead(new ZipInputStream(bis));
        testRead(new GZIPInputStream(bis1));
    }
}

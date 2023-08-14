public class GZIPInputStreamRead {
    public static void main(String[] args) throws Throwable {
        Random rnd = new Random();
        for (int i = 1; i < 100; i++) {
            int members = rnd.nextInt(10) + 1;
            ByteArrayOutputStream srcBAOS = new ByteArrayOutputStream();
            ByteArrayOutputStream dstBAOS = new ByteArrayOutputStream();
            for (int j = 0; j < members; j++) {
                byte[] src = new byte[rnd.nextInt(8192) + 1];
                rnd.nextBytes(src);
                srcBAOS.write(src);
                try (GZIPOutputStream gzos = new GZIPOutputStream(dstBAOS)) {
                    gzos.write(src);
                }
            }
            byte[] srcBytes = srcBAOS.toByteArray();
            byte[] dstBytes = dstBAOS.toByteArray();
            for (int j = 0; j < 10; j++) {
                int readBufSZ = rnd.nextInt(2048) + 1;
                test(srcBytes,
                     dstBytes,
                     readBufSZ,
                     512);    
                test(srcBytes,
                     dstBytes,
                     readBufSZ,
                     rnd.nextInt(4096) + 1);
            }
        }
    }
    private static void test(byte[] src, byte[] dst,
                             int readBufSize, int gzisBufSize)
        throws Throwable
    {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(dst);
             GZIPInputStream gzis = new GZIPInputStream(bais, gzisBufSize))
        {
            byte[] result = new byte[src.length + 10];
            byte[] buf = new byte[readBufSize];
            int n = 0;
            int off = 0;
            while ((n = gzis.read(buf, 0, buf.length)) != -1) {
                System.arraycopy(buf, 0, result, off, n);
                off += n;
            }
            if (off != src.length || gzis.available() != 0 ||
                !Arrays.equals(src, Arrays.copyOf(result, off))) {
                throw new RuntimeException(
                    "GZIPInputStream reading failed! " +
                    ", src.len=" + src.length +
                    ", read=" + off);
            }
        }
    }
}

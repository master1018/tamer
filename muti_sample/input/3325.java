public class LargeFileAvailable {
    private static final long FILESIZE = 7405576182L;
    public static void main(String args[]) throws Exception {
        File file = createLargeFile(FILESIZE);
        try (FileInputStream fis = new FileInputStream(file)) {
            if (file.length() != FILESIZE) {
                throw new RuntimeException("unexpected file size = " + file.length());
            }
            long bigSkip = 3110608882L;
            long remaining = FILESIZE;
            remaining -= skipBytes(fis, bigSkip, remaining);
            remaining -= skipBytes(fis, 10L, remaining);
            remaining -= skipBytes(fis, bigSkip, remaining);
            if (fis.available() != (int) remaining) {
                 throw new RuntimeException("available() returns " +
                     fis.available() +
                     " but expected " + remaining);
            }
        } finally {
            file.delete();
        }
    }
    private static long skipBytes(InputStream is, long toSkip, long avail)
            throws IOException {
        long skip = is.skip(toSkip);
        if (skip != toSkip) {
            throw new RuntimeException("skip() returns " + skip +
                " but expected " + toSkip);
        }
        long remaining = avail - skip;
        int expected = remaining >= Integer.MAX_VALUE
                           ? Integer.MAX_VALUE
                           : (int) remaining;
        System.out.println("Skipped " + skip + " bytes " +
            " available() returns " + expected +
            " remaining=" + remaining);
        if (is.available() != expected) {
            throw new RuntimeException("available() returns " +
                is.available() + " but expected " + expected);
        }
        return skip;
    }
    private static File createLargeFile(long filesize) throws Exception {
        File largefile = File.createTempFile("largefile", null);
        Files.delete(largefile.toPath());
        try (FileChannel fc =
                FileChannel.open(largefile.toPath(),
                                 CREATE_NEW, WRITE, SPARSE)) {
            ByteBuffer bb = ByteBuffer.allocate(1).put((byte)1);
            bb.rewind();
            int rc = fc.write(bb, filesize-1);
            if (rc != 1) {
                throw new RuntimeException("Failed to write 1 byte to the large file");
            }
        }
        return largefile;
    }
}

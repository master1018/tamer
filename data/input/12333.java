public class TransferToChannel {
    static File file;
    static File outFile;
    static FileChannel in;
    static int CHUNK_SIZE = 1024 * 9;
    public static void main(String[] args) throws Exception {
        file = File.createTempFile("readingin", null);
        outFile = File.createTempFile("writingout", null);
        file.deleteOnExit();
        outFile.deleteOnExit();
        generateBigFile(file);
        FileInputStream fis = new FileInputStream(file);
        in = fis.getChannel();
        test1();
        test2();
        in.close();
        file.delete();
        outFile.delete();
    }
    static void test1() throws Exception {
        for (int i=0; i<10; i++) {
            transferFileToUserChannel();
            System.gc();
            System.err.println("Transferred file...");
        }
    }
    static void test2() throws Exception {
        for (int i=0; i<10; i++) {
            transferFileToTrustedChannel();
            System.gc();
            System.err.println("Transferred file...");
        }
    }
    static void transferFileToUserChannel() throws Exception {
        long remainingBytes = in.size();
        long size = remainingBytes;
        WritableByteChannel wbc = new WritableByteChannel() {
                Random rand = new Random(0);
                public int write(ByteBuffer src) throws IOException {
                    int read = src.remaining();
                    byte[] incoming = new byte[read];
                    src.get(incoming);
                    checkData(incoming, read);
                    return read == 0 ? -1 : read;
                }
                public boolean isOpen() {
                    return true;
                }
                public void close() throws IOException {
                }
                void checkData(byte[] incoming, int size) {
                    byte[] expected = new byte[size];
                    rand.nextBytes(expected);
                    for (int i=0; i<size; i++)
                        if (incoming[i] != expected[i])
                            throw new RuntimeException("Data corrupted");
                }
            };
        while (remainingBytes > 0) {
            long bytesTransferred = in.transferTo(size - remainingBytes,
                              Math.min(CHUNK_SIZE, remainingBytes), wbc);
            if (bytesTransferred >= 0)
                remainingBytes -= bytesTransferred;
            else
                throw new Exception("transfer failed");
        }
    }
    static void transferFileToTrustedChannel() throws Exception {
        long remainingBytes = in.size();
        long size = remainingBytes;
        FileOutputStream fos = new FileOutputStream(outFile);
        FileChannel out = fos.getChannel();
        while (remainingBytes > 0) {
            long bytesTransferred = in.transferTo(size - remainingBytes,
                                                  CHUNK_SIZE, out);
            if (bytesTransferred >= 0)
                remainingBytes -= bytesTransferred;
            else
                throw new Exception("transfer failed");
        }
        out.close();
    }
    static void generateBigFile(File file) throws Exception {
        OutputStream out = new BufferedOutputStream(
                           new FileOutputStream(file));
        byte[] randomBytes = new byte[1024];
        Random rand = new Random(0);
        for (int i = 0; i < 1000; i++) {
            rand.nextBytes(randomBytes);
            out.write(randomBytes);
        }
        out.flush();
        out.close();
    }
}

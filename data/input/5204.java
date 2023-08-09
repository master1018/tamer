public class ReadBytesBounds {
    static final FileInputStream fis;
    static final RandomAccessFile raf;
    static final byte[] b = new byte[32];
    static {
        try {
            String dir = System.getProperty("test.src", ".");
            File testFile = new File(dir, "input.txt");
            fis = new FileInputStream(testFile);
            raf = new RandomAccessFile(testFile , "r");
        } catch (Throwable t) {
            throw new Error(t);
        }
    }
    public static void main(String argv[]) throws Throwable {
        try {
            testRead(-1, -1, false);
            testRead(-1,  0, false);
            testRead( 0, -1, false);
            testRead( 0, 33, false);
            testRead(33,  0, false);
            testRead(33,  4, false);
            testRead( 0, 32, true);
            testRead(32,  0, true);
            testRead(32,  4, false);
            testRead( 4, 16, true);
            testRead( 1, 31, true);
            testRead( 0,  0, true);
            testRead(31,  Integer.MAX_VALUE, false);
            testRead( 0,  Integer.MAX_VALUE, false);
            testRead(-1,  Integer.MAX_VALUE, false);
            testRead(-4,  Integer.MIN_VALUE, false);
            testRead( 0,  Integer.MIN_VALUE, false);
        } finally {
            fis.close();
            raf.close();
        }
    }
    static void testRead(int off, int len, boolean expected) throws Throwable {
        System.err.printf("off=%d len=%d expected=%b%n", off, len, expected);
        boolean result;
        try {
            fis.read(b, off, len);
            raf.read(b, off, len);
            result = true;
        } catch (IndexOutOfBoundsException e) {
            result = false;
        }
        if (result != expected) {
            throw new RuntimeException
                (String.format("Unexpected result off=%d len=%d expected=%b",
                               off, len, expected));
        }
    }
}

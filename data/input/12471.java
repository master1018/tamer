public class ReadZero {
    public static void main(String [] args) throws IOException {
        ReadZero r = new ReadZero();
        r.testInputStream();
    }
    private void testInputStream() throws IOException {
        File f = new File(System.getProperty("test.src", "."), "ReadZero.java");
        InputStream is = new FileInputStream(f) {
            public int read(byte [] b, int off, int len) {
                System.out.println("FileInputStream.read");
                return 0;
            }
        };
        try {
            is.read(new byte[1], 0, 1); 
            InputStreamReader isr = new InputStreamReader(is);
            try {
                int res = isr.read(new char[1], 0, 1);
            } catch (IOException x) {
                System.out.println("IOException caught");
                return;
            }
        } finally {
            is.close();
        }
        throw new RuntimeException("IOException not thrown");
    }
}

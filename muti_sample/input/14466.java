public class FinalizeShdCallClose {
    static final String FILE_NAME = "empty.txt";
    public static class MyStream extends FileInputStream {
        private boolean closed = false;
        public MyStream(String name) throws FileNotFoundException {
            super(name);
        }
        public void finalize() {
            try {
                super.finalize();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        public void close() {
            try {
                super.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            closed = true;
        }
        public boolean isClosed() {
            return closed;
        }
    }
    public static void main(String argv[]) throws Exception {
        File inFile= new File(System.getProperty("test.dir", "."), FILE_NAME);
        inFile.createNewFile();
        inFile.deleteOnExit();
        String name = inFile.getPath();
        MyStream ms = null;
        try {
            ms = new MyStream(name);
        } catch (FileNotFoundException e) {
            System.out.println("Unexpected exception " + e);
            throw(e);
        }
        ms.finalize();
        if (!ms.isClosed()) {
            throw new Exception("MyStream.close() method is not called");
        }
        System.out.println("OK");
    }
}

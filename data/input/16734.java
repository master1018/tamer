public class Args {
    static void fail(String s) {
        throw new RuntimeException(s);
    }
    static interface Thunk {
        public void run() throws Exception;
    }
    private static void tryCatch(Class ex, Thunk thunk) {
        boolean caught = false;
        try {
            thunk.run();
        } catch (Throwable x) {
            if (ex.isAssignableFrom(x.getClass())) {
                caught = true;
                System.err.println("Thrown as expected: " + x);
            }
        }
        if (!caught)
            fail(ex.getName() + " not thrown");
    }
    public static void main(String[] args) throws Exception {
        File f = File.createTempFile("foo", null);
        f.deleteOnExit();
        final FileChannel fc = new RandomAccessFile(f, "rw").getChannel();
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.transferFrom(fc, -1, 1);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.transferFrom(fc, 0, -1);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.transferTo(-1, 1, fc);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.transferTo(0, -1, fc);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.map(FileChannel.MapMode.READ_ONLY, -1, 0);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.map(FileChannel.MapMode.READ_ONLY, 0, -1);
                }});
        tryCatch(IllegalArgumentException.class, new Thunk() {
                public void run() throws Exception {
                    fc.map(FileChannel.MapMode.READ_ONLY, 0,
                           (long)Integer.MAX_VALUE << 3);
                }});
        fc.close();
        f.delete();
    }
}

public class EmptyCharsetName {
    static boolean compat;
    static abstract class Test {
        public abstract void go() throws Exception;
        Test() throws Exception {
            try {
                go();
            } catch (Exception x) {
                if (compat) {
                    if (x instanceof UnsupportedCharsetException) {
                        System.err.println("Thrown as expected: " + x);
                        return;
                    }
                    throw new Exception("Exception thrown", x);
                }
                if (x instanceof IllegalCharsetNameException) {
                    System.err.println("Thrown as expected: " + x);
                    return;
                }
                throw new Exception("Incorrect exception: "
                                    + x.getClass().getName(),
                                    x);
            }
            if (!compat)
                throw new Exception("No exception thrown");
        }
    }
    public static void main(String[] args) throws Exception {
        String bl = System.getProperty("sun.nio.cs.bugLevel");
        compat = (bl != null && bl.equals("1.4"));
        new Test() {
                public void go() throws Exception {
                    Charset.forName("");
                }};
        new Test() {
                public void go() throws Exception {
                    Charset.isSupported("");
                }};
        new Test() {
                public void go() throws Exception {
                    new Charset("", new String[] { }) {
                            public CharsetDecoder newDecoder() {
                                return null;
                            }
                            public CharsetEncoder newEncoder() {
                                return null;
                            }
                            public boolean contains(Charset cs) {
                                return false;
                            }
                        };
                }};
    }
}

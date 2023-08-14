public class WriteParams {
    static int values[] = {Integer.MIN_VALUE, -1, 0, 1, 4, 16, 31,
                           32, 33, Integer.MAX_VALUE};
    static char b[][] = {null, new char[32]};
    static void test(Writer wtr) throws Exception {
        int i = 0, j = 0, k = 0;
        boolean nullPtr = false, indexOutBnd = false;
        for (i = 0; i < b.length; i++) {
            for ( j = 0; j < values.length; j++) {
                for ( k = 0; k < values.length; k++) {
                    nullPtr = (b[i] == null);
                    int bufLen = nullPtr ? 0 : b[i].length;
                    indexOutBnd =  ((values[j] + values[k]) < 0)
                        || (values[j] < 0)
                        || (values[j] > bufLen)
                        || (values[k] < 0)
                        || ((values[j] + values[k]) > bufLen);
                    try {
                        wtr.write(b[i], values[j], values[k]);
                    } catch (NullPointerException e) {
                        if (!nullPtr) {
                            throw new Exception
                                ("should not throw NullPointerException");
                        }
                        continue;
                    } catch (IndexOutOfBoundsException e) {
                        if (!indexOutBnd) {
                            throw new Exception
                                ("should not throw IndexOutOfBoundsException");
                        }
                        continue;
                    }
                    if (nullPtr || indexOutBnd) {
                        throw new Exception("Should have thrown an exception");
                    }
                }
            }
        }
    }
    public static void main(String args[]) throws Exception{
        StringWriter sw = new StringWriter();
        test(sw);
        test(new BufferedWriter(sw));
        test(new CharArrayWriter());
        test(new OutputStreamWriter(System.err));
        test(new PipedWriter(new PipedReader()));
    }
}

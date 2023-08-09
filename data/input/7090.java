public class ReadAhead {
    static private class LimitedInputStream extends InputStream {
        private String input;
        private int limit;      
        private int next = 0;
        public LimitedInputStream(String input, int limit) {
            this.input = input;
            this.limit = limit;
        }
        public int read() throws IOException {
            if (next >= limit)
                throw new IOException("Attempted to read too far in stream");
            return input.charAt(next++);
        }
    }
    static private class LimitedReader extends Reader {
        private String input;
        private int limit;      
        private int next = 0;
        public LimitedReader(String input, int limit) {
            this.input = input;
            this.limit = limit;
        }
        public int read() throws IOException {
            if (next >= limit)
                throw new IOException("Attempted to read too far in stream");
            return input.charAt(next++);
        }
        public int read(char[] b, int off, int len) throws IOException {
            int top = off + len;
            int i;
            for (i = off; i < top; i++) {
                int c = read();
                if (c < 0) break;
                b[i] = (char)c;
            }
            return i - off;
        }
        public void close() { }
    }
    static private interface StreamTokenizerMaker {
        public StreamTokenizer create(String input, int limit);
    }
    static private void fail(String why) throws Exception {
        throw new Exception(why);
    }
    private static void test(StreamTokenizer st) throws Exception {
        st.eolIsSignificant(true);
        int tt = st.nextToken();
        if (tt != StreamTokenizer.TT_WORD) fail("expected TT_WORD");
        if (!st.sval.equals("foo")) fail("expected word token \"foo\"");
        tt = st.nextToken();
        if (tt != StreamTokenizer.TT_EOL) fail("expected TT_EOL");
    }
    private static void test(StreamTokenizerMaker stm) throws Exception {
        test(stm.create("foo\nx", 4));
        test(stm.create("foo\r\nx", 4));
    }
    public static void main(String[] args) throws Exception {
        test(new StreamTokenizerMaker() {
            public StreamTokenizer create(String input, int limit) {
                return new StreamTokenizer(new LimitedInputStream(input, limit));
            }});
        test(new StreamTokenizerMaker() {
            public StreamTokenizer create(String input, int limit) {
                return new StreamTokenizer(new LimitedReader(input, limit));
            }});
    }
}

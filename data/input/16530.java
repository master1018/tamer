public class Ready {
    public static void main(String[] args) throws IOException {
        BufferedReader reader;
        String[] strings = {
            "LF-Only\n",
            "LF-Only\n",
            "CR/LF\r\n",
            "CR/LF\r\n",
            "CR-Only\r",
            "CR-Only\r",
            "CR/LF line\r\nMore data.\r\n",
            "CR/LF line\r\nMore data.\r\n"
        };
        int[] bufsizes = { 7, 8, 6, 5, 7, 8, 11, 10};
        for (int i = 0; i < strings.length; i++) {
            reader = new BufferedReader(new BoundedReader(strings[i]),
                    bufsizes[i]);
            while (reader.ready()) {
                String str = reader.readLine();
                System.out.println("read>>" + str);
            }
        }
    }
    private static class BoundedReader extends Reader{
        private char[] content;
        private int limit;
        private int pos = 0;
        public BoundedReader(String content) {
            this.limit = content.length();
            this.content = new char[limit];
            content.getChars(0, limit, this.content, 0);
        }
        public int read() throws IOException {
            if (pos >= limit)
                throw new RuntimeException("Hit infinite wait condition");
            return content[pos++];
        }
        public int read(char[] buf, int offset, int length)
            throws IOException
        {
            if (pos >= limit)
                throw new RuntimeException("Hit infinite wait condition");
            int oldPos = pos;
            int readlen = (length > (limit - pos)) ? (limit - pos) : length;
            for (int i = offset; i < readlen; i++) {
                buf[i] = (char)read();
            }
            return (pos - oldPos);
        }
        public void close() {}
        public boolean ready() {
            if (pos < limit)
                return true;
            else
                return false;
        }
    }
}

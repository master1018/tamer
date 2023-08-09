public class BufferSizes {
    static int min = 90;
    static int max = 110;
    static int chunk = 100;
    static int count = 1000;
    static void runBytes() throws IOException {
        for (int sz = min; sz <= max; sz++) {
            System.err.println(sz);
            InputStream in
                = new BufferedInputStream(new ABCInputStream(count, chunk), sz);
            OutputStream out
                = new BufferedOutputStream(new ABCOutputStream(count), sz);
            int n;
            byte[] buf = new byte[sz];
            while ((n = in.read(buf, 0, sz)) != -1)
                out.write(buf, 0, n);
            in.close();
            out.close();
        }
    }
    static void runChars() throws IOException {
        for (int sz = min; sz <= max; sz++) {
            System.err.println(sz);
            Reader in
                = new BufferedReader(new InputStreamReader(new ABCInputStream(count, chunk)), sz);
            Writer out
                = new BufferedWriter(new OutputStreamWriter(new ABCOutputStream(count)), sz);
            int n;
            char[] cbuf = new char[sz];
            while ((n = in.read(cbuf, 0, sz)) != -1)
                out.write(cbuf, 0, n);
            in.close();
            out.close();
        }
    }
    public static void main(String[] args) throws IOException {
        runBytes();
        runChars();
    }
}

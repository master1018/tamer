public class BCat {
    public static int count = 10000;
    public static int chunk = 512;
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new ABCInputStream(count, chunk)),
                                               1025);
        Writer out = new BufferedWriter(new OutputStreamWriter(new ABCOutputStream(count)));
        char[] buf = new char[119];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            System.err.print(" " + n);
        }
        in.close();
        out.close();
        System.err.println();
    }
}

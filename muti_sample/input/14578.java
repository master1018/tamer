public class ReadReadLine {
    public static void main(String[] args) throws Exception {
        test("\r\n", 1);
        test("\r\r\n", 2);
        test("\r\n\n\n", 3);
    }
    static void test(String s, int good) throws Exception {
        int c, line;
        LineNumberReader r = new LineNumberReader(new StringReader(s), 2);
        if ((c = r.read()) >= 0) {
            while (r.readLine() != null)
                ;
        }
        line = r.getLineNumber();
        if(line != good) {
            throw new Exception("Failed test: Expected line number "
                                + good + " Got: " + line);
        }
    }
}

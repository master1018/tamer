public class MarkedFillAtEOF {
    public static void main(String[] args) throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("12"));
        int count = 0;
        r.read();
        r.mark(2);
        while (r.read() != -1);
        r.reset();
        while (r.read() != -1) {
            count++;
        }
        if (count != 1) {
            throw new Exception("Expect 1 character, but got " + count);
        }
    }
}

public class ReadToArray {
    public static void main(String[] args) throws Exception {
        PipedWriter pw = new PipedWriter();
        PipedReader pr = new PipedReader(pw);
        char[] cbuf = {'a', 'a', 'a', 'a'};
        pw.write('b');
        pr.read(cbuf, 2, 1);
        if (cbuf[2] != 'b') {
            throw new Exception
            ("Read character to wrong position: 2nd character should be b");
        }
    }
}

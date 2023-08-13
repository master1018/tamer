public class WriteFromString {
    public static void main(String argv[]) throws Exception {
        LocalStringWriter lsw = new LocalStringWriter();
        boolean result = true;
        String testString = "Testing of what gets written";
        lsw.write(testString, 1, 4);
        String res = lsw.toString();
        if (!res.equals("esti")) {
            result = false;
            System.err.println("Writer.write is incorrect:" + res);
        }
        StringWriter sw = new StringWriter();
        sw.write(testString, 1, 4);
        res = sw.toString();
        String ss = testString.substring(1,4);
        System.out.println("Substring = "+ss);
        if (!res.equals("esti")) {
            System.err.println("StringWriter.write is incorrect:" + res);
            result = false;
        }
        if (!result) {
            throw new Exception("Writer.write method is incorrect.");
        }
    }
}
class LocalStringWriter extends Writer {
    private StringBuffer buf;
    public LocalStringWriter() {
        buf = new StringBuffer();
        lock = buf;
    }
    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(cbuf, off, len);
    }
    public void write(String str) {
        buf.append(str);
    }
    public String toString() {
        return buf.toString();
    }
    public void flush(){ }
    public void close(){ }
}

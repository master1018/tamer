public class FullRead {
    static int MAX_LEN = 1 << 16;
    static void test(File f, int len) throws Exception {
        FileOutputStream fo = new FileOutputStream(f);
        for (int i = 0; i < len; i++)
            fo.write('x');
        fo.close();
        FileInputStream fi = new FileInputStream(f);
        Reader rd = new InputStreamReader(fi, "US-ASCII");
        char[] cb = new char[MAX_LEN + 100];
        int n = rd.read(cb, 0, cb.length);
        System.out.println(len + " : " + n);
        if (len != n)
            throw new Exception("Expected " + len + ", read " + n);
    }
    public static void main(String[] args) throws Exception {
        File f = File.createTempFile("foo", "bar");
        f.deleteOnExit();
        System.out.println(f);
        for (int i = 4; i <= MAX_LEN; i <<= 1)
            test(f, i);
    }
}

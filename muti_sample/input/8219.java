public class TinyBuffers {
    private static Charset cs = Charset.forName("UTF-16");
    private static void test(int sz) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[100]);
        ReadableByteChannel ch = Channels.newChannel(bis);
        Reader r = Channels.newReader(ch, cs.newDecoder(), sz);
        char [] arr = new char[100];
        System.out.println(r.read(arr, 0, arr.length));
    }
    public static void main(String[] args) throws Exception {
        for (int i = -2; i < 10; i++)
            test(i);
    }
}

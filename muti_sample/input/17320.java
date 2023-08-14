public class EncDec {
    public static void main(String[] args) throws Exception {
        String s = "Hello, world!";
        ByteBuffer bb = ByteBuffer.allocate(100);
        bb.put(Charset.forName("ISO-8859-15").encode(s)).flip();
        String t = Charset.forName("UTF-8").decode(bb).toString();
        System.err.println(t);
        if (!t.equals(s))
            throw new Exception("Mismatch: " + s + " != " + t);
    }
}

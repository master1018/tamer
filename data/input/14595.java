public class DecoderOverflow {
    static int failures = 0;
    public static void main(String[] args) throws Exception {
        for (String csn : Charset.availableCharsets().keySet()) {
            try {
                test(csn);
            } catch (Throwable t) {
                System.out.println(csn);
                t.printStackTrace();
                failures++;
            }
        }
        if (failures > 0)
            throw new Exception(failures + " charsets failed");
    }
    static void test(String encoding) throws Exception {
        String text = "Vote for Duke!";
        Charset cs = Charset.forName(encoding);
        if (! cs.canEncode() || ! cs.newEncoder().canEncode('.')) return;
        ByteBuffer in = ByteBuffer.wrap(text.getBytes(encoding));
        CharBuffer out = CharBuffer.allocate(text.length()/2);
        CoderResult result = cs.newDecoder().decode(in, out, true);
        if (out.hasRemaining() || ! result.isOverflow())
            throw new Exception
                ("out.hasRemaining()=" + out.hasRemaining() +
                 " result.isOverflow()=" + result.isOverflow() +
                 " in.capacity()=" + in.capacity() +
                 " encoding=" + encoding);
    }
}

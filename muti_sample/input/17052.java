public class Surrogates {
    static PrintStream log = System.err;
    static char[] input;
    static byte[] output;
    static final int LEN = 128;
    static void initData() throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < LEN; i++) {
            int c = Character.MIN_SUPPLEMENTARY_CODE_POINT + 1;
            sb.append(Character.toChars(c));
        }
        input = sb.toString().toCharArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw
            = new OutputStreamWriter(bos, Charset.forName("UTF-8"));
        osw.write(input);
        osw.close();
        output = bos.toByteArray();
    }
    static void testLeftovers(boolean doMalformed)
        throws Exception
    {
        log.print("Leftover surrogates, doMalformed = " + doMalformed);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw
            = new OutputStreamWriter(bos, Charset.forName("UTF-8"));
        for (int i = 0; i < input.length; i += 7)
            osw.write(input, i, Math.min(input.length - i, 7));
        if (doMalformed)
            osw.write(input, 0, 1);
        osw.close();
        byte[] result = bos.toByteArray();
        int rl = result.length + (doMalformed ? -1 : 0);
        if (rl != output.length)
            throw new Exception("Incorrect result length "
                                + rl + ", expected " + output.length);
        for (int i = 0; i < output.length; i++)
            if (result[i] != output[i])
                throw new Exception("Unexpected result value at index " + i);
        log.println(": Passed");
    }
    public static void main(String[] args) throws Exception {
        initData();
        testLeftovers(false);
        testLeftovers(true);
    }
}

public class IOCoders {
    static Charset ascii = Charset.forName("US-ASCII");
    static void isrPositive() throws Exception {
        ByteArrayInputStream bis
            = new ByteArrayInputStream(new byte[] { (byte)'h', (byte)'i' });
        InputStreamReader isr
            = new InputStreamReader(bis,
                                    ascii.newDecoder()
                                    .onMalformedInput(CodingErrorAction.REPORT)
                                    .onUnmappableCharacter(CodingErrorAction.REPORT));
        BufferedReader br = new BufferedReader(isr);
        if (!br.readLine().equals("hi"))
            throw new Exception();
    }
    static void isrNegative() throws Exception {
        ByteArrayInputStream bis
            = new ByteArrayInputStream(new byte[] { (byte)0xff, (byte)0xff });
        InputStreamReader isr
            = new InputStreamReader(bis,
                                    ascii.newDecoder()
                                    .onMalformedInput(CodingErrorAction.REPORT)
                                    .onUnmappableCharacter(CodingErrorAction.REPORT));
        BufferedReader br = new BufferedReader(isr);
        try {
            br.readLine();
        } catch (MalformedInputException x) {
            return;
        }
        throw new Exception();
    }
    static void oswPositive() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw
            = new OutputStreamWriter(bos,
                                     ascii.newEncoder()
                                     .onMalformedInput(CodingErrorAction.REPORT)
                                     .onUnmappableCharacter(CodingErrorAction.REPORT));
        osw.write("hi");
        osw.close();
        if (!ascii.decode(ByteBuffer.wrap(bos.toByteArray()))
            .toString().equals("hi"))
            throw new Exception();
    }
    static void oswNegative() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw
            = new OutputStreamWriter(bos,
                                     ascii.newEncoder()
                                     .onMalformedInput(CodingErrorAction.REPORT)
                                     .onUnmappableCharacter(CodingErrorAction.REPORT));
        try {
            osw.write("\u00A0\u00A1");
        } catch (UnmappableCharacterException x) {
            return;
        }
        throw new Exception();
    }
    public static void main(String[] args) throws Exception {
        isrPositive();
        isrNegative();
        oswPositive();
        oswNegative();
    }
}

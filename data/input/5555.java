public class SJISCanEncode {
    private Charset cs;
    private CharsetEncoder encoder;
    private void canEncodeTest(char inputChar,
                               boolean expectedResult)
                               throws Exception {
        String msg = "err: Shift_JIS canEncode() return value ";
        if (encoder.canEncode(inputChar) != expectedResult) {
            throw new Exception(msg + !(expectedResult) +
                ": "  + Integer.toHexString((int)inputChar));
        }
    }
    public static void main(String[] args) throws Exception {
        SJISCanEncode test = new SJISCanEncode();
        test.cs = Charset.forName("SJIS");
        test.encoder = test.cs.newEncoder();
        test.canEncodeTest('\u0001', true);
        test.canEncodeTest('\uFF01', true);
        test.canEncodeTest('\u4E9C', true);
        test.canEncodeTest('\u3041', true);
        test.canEncodeTest('\u30A1', true);
        test.canEncodeTest('\u0080', false);
        test.canEncodeTest('\u4000', false);
    }
}

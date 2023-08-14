@TestTargetClass(targets.Charsets._Abstract.class)
public class Charset_SingleByteAbstractTest extends Charset_AbstractTest {
    static byte[] allBytes;
    static char[] allChars;
    @Override
    protected void setUp() throws Exception {
        allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public static void dumpDecoded () {
        Charset_TestGenerator.Dumper out = new Charset_TestGenerator.Dumper1();
        ByteBuffer inputBB = ByteBuffer.wrap(allBytes);
        CharBuffer outputCB;
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        try {
            outputCB = decoder.decode(inputBB);
            outputCB.rewind();
            while (outputCB.hasRemaining()) {
                out.consume(outputCB.get());
            }
        } catch (CharacterCodingException e) {
            System.out.println(e);
        }
    }
    public static void decodeReplace (byte[] input, char[] expectedOutput) throws CharacterCodingException {
        ByteBuffer inputBB = ByteBuffer.wrap(input);
        CharBuffer outputCB;
        decoder.onMalformedInput(CodingErrorAction.REPLACE);
        outputCB = decoder.decode(inputBB);
        outputCB.rewind();
        assertEqualChars2("Decoded charactes must match!",
                expectedOutput,
                outputCB.array(),
                input);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "functionalCoDec_REPR",
        args = {}
    )
    @Override
    public void test_Decode () throws CharacterCodingException {
        decodeReplace(allBytes, allChars);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "functionalCoDec_REPR",
        args = {}
    )
    @Override
    public void test_Encode () throws CharacterCodingException {
        CharBuffer inputCB = CharBuffer.wrap(allChars);
        ByteBuffer outputBB;
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        outputBB = encoder.encode(inputCB);
        outputBB.rewind();
        assertEqualBytes2("Encoded bytes must match!", allBytes, outputBB.array(), allChars);
    }
    static void assertEqualChars2 (String msg, char[] expected, char[] actual, byte[] bytes) {
        boolean match = true;
        boolean replaceMatch = true;
        int len = expected.length;
        if (actual.length < len) len = actual.length;
        for (int i = 0; i < len; i++) {
            if (actual[i] == expected[i]) {
            }
            else {
                if (expected[i] == 65533) {
                    if (actual[i] == (bytes[i] & 0xff)) {
                    } else {
                    }
                    replaceMatch = false;
                } else {
                    match = false;
                }
            }
        }
        assertTrue(msg, match);
        if (!replaceMatch) {
        }
    }
    static void assertEqualBytes2 (String msg, byte[] expected, byte[] actual, char[] chars) {
        boolean match = true;
        int len = expected.length;
        if (actual.length < len) len = actual.length;
        for (int i = 0; i < len; i++) {
            if ((actual[i] != expected[i]) &&
                    !((chars[i] == 65533)) && (actual[i] == 63)) {
                match = false;
            }
        }
        assertTrue(msg, match);
    }
    public static void main(String[] args) {
        dumpDecoded();
    }
}

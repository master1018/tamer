public class PCM_FLOAT_support {
    static Encoding pcmFloatEnc;
    static boolean testFailed = false;
    public static void main(String[] args) throws Exception {
        pcmFloatEnc = Encoding.PCM_FLOAT;
        Encoding[] encodings = AudioSystem.getTargetEncodings(pcmFloatEnc);
        out("conversion from PCM_FLOAT to " + encodings.length + " encodings:");
        for (Encoding e: encodings) {
            out("  - " + e);
        }
        if (encodings.length == 0) {
            testFailed = true;
        }
        test(Encoding.PCM_SIGNED);
        test(Encoding.PCM_UNSIGNED);
        if (testFailed) {
            throw new Exception("test failed");
        }
        out("test passed.");
    }
    static void out(String s) {
        System.out.println(s);
    }
    static boolean test(Encoding enc) {
        out("conversion " + enc + " -> PCM_FLOAT:");
        Encoding[] encodings = AudioSystem.getTargetEncodings(enc);
        for (Encoding e: encodings) {
            if (e.equals(pcmFloatEnc)) {
                out("  - OK");
                return true;
            }
        }
        out("  - FAILED (not supported)");
        testFailed = true;
        return false;
    }
}

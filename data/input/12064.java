public class Matches_NOT_SPECIFIED {
    static boolean success = true;
    static AudioFormat f1;
    static AudioFormat f2;
    public static void main(String[] args) throws Exception {
        AudioFormat f3;
        f1 = new AudioFormat(44100, 16, 2, true, false);
        f2 = new AudioFormat(Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, false);
        test(true);
        f2 = new AudioFormat(Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, true);
        test(false);
        f1 = new AudioFormat(44100, 8, 8, true, false);
        test(true);
        if (success) {
            out("The test PASSED.");
        } else {
            out("The test FAILED.");
            throw new Exception("The test FAILED");
        }
    }
    static void test(boolean shouldMatch) {
        out("testing:");
        out("  - " + f1.toString());
        out("  - " + f2.toString());
        if (f1.matches(f2)) {
            if (shouldMatch) {
                out("  (OK) MATCHES");
            } else {
                out("  (ERROR) MATCHES");
                success = false;
            }
        } else {
            if (shouldMatch) {
                out("  (ERROR) DOESNT MATCH!");
                success = false;
            } else {
                out("  (OK) DOESNT MATCH!");
            }
        }
    }
    static void out(String s) {
        System.out.println(s);
    }
}

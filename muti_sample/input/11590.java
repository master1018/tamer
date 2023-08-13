public class Bug6665028 {
    private static boolean runrun = true;
    private static class Test extends Thread {
        public void run() {
            while (runrun) {
                test1();
                test2();
            }
        }
    }
    public static void main(String[] args) {
        int duration = 45;
        if (args.length == 1) {
            duration = Math.max(1, Math.min(Integer.parseInt(args[0]), 45));
        }
        Test[] tests = new Test[4];
        for (int i = 0; i < tests.length; i++) {
            Test t = new Test();
            tests[i] = t;
            t.start();
        }
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
        }
        runrun = false;
        for (int i = 0; i < tests.length; i++) {
            try {
                tests[i].join();
            } catch (InterruptedException e) {
            }
        }
    }
    static String target;
    static {
        String s = "A Bidi object provides information on the bidirectional reordering of the text used to create it. This is required, for example, to properly display Arabic or Hebrew text. ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(s);
        }
        target = sb.toString();
    }
    static void test1() {
        String str = "If this text is >" + target + "< the test passed.";
        int start = str.indexOf(target);
        int limit = start + target.length();
        AttributedString astr = new AttributedString(str);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING,
                         new Integer(-1),
                         start,
                         limit);
        Bidi bidi = new Bidi(astr.getIterator());
        byte[] embs = new byte[str.length() + 3];
        for (int i = start + 1; i < limit + 1; ++i) {
            embs[i] = -1;
        }
        Bidi bidi2 = new Bidi(str.toCharArray(), 0, embs, 1, str.length(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        if (bidi.getRunCount() != 3 || bidi2.getRunCount() != 3) {
            throw new Error("Bidi run count incorrect");
        }
    }
    static void test2() {
        String str = "If this text is >" + target + "< the test passed.";
        int length = str.length();
        int start = str.indexOf(target);
        int limit = start + target.length();
        AttributedString astr = new AttributedString(str);
        astr.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING,
                         new Integer(-3),
                         start,
                         limit);
        Bidi bidi = new Bidi(astr.getIterator());
        if (bidi.getRunCount() != 6) { 
            throw new Error("Bidi embedding processing failed");
        }
    }
}

public class BidiEmbeddingTest {
    public static void main(String[] args) {
        if (args.length > 0) {
            Frame f = new Frame();
            f.setSize(300, 300);
            f.setBackground(Color.white);
            f.show();
        }
        test1();
        test2();
    }
    static void test1() {
        String target = "BACK WARDS";
        String str = "If this text is >" + target + "< the test passed.";
        int start = str.indexOf(target);
        int limit = start + target.length();
        System.out.println("start: " + start + " limit: " + limit);
        AttributedString astr = new AttributedString(str);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING,
                         new Integer(-1),
                         start,
                         limit);
        Bidi bidi = new Bidi(astr.getIterator());
        for (int i = 0; i < bidi.getRunCount(); ++i) {
            System.out.println("run " + i +
                               " from " + bidi.getRunStart(i) +
                               " to " + bidi.getRunLimit(i) +
                               " at level " + bidi.getRunLevel(i));
        }
        System.out.println(bidi);
        byte[] embs = new byte[str.length() + 3];
        for (int i = start + 1; i < limit + 1; ++i) {
            embs[i] = -1;
        }
        Bidi bidi2 = new Bidi(str.toCharArray(), 0, embs, 1, str.length(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        for (int i = 0; i < bidi2.getRunCount(); ++i) {
            System.out.println("run " + i +
                               " from " + bidi2.getRunStart(i) +
                               " to " + bidi2.getRunLimit(i) +
                               " at level " + bidi2.getRunLevel(i));
        }
        System.out.println(bidi2 + "\n");
        if (bidi.getRunCount() != 3 || bidi2.getRunCount() != 3) {
            throw new Error("Bidi run count incorrect");
        } else {
            System.out.println("test1() passed.\n");
        }
    }
    static void test2() {
        String target = "BACK WARDS";
        String str = "If this text is >" + target + "< the test passed.";
        int length = str.length();
        int start = str.indexOf(target);
        int limit = start + target.length();
        System.out.println("start: " + start + " limit: " + limit);
        AttributedString astr = new AttributedString(str);
        astr.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        astr.addAttribute(TextAttribute.BIDI_EMBEDDING,
                         new Integer(-3),
                         start,
                         limit);
        Bidi bidi = new Bidi(astr.getIterator());
        for (int i = 0; i < bidi.getRunCount(); ++i) {
            System.out.println("run " + i +
                               " from " + bidi.getRunStart(i) +
                               " to " + bidi.getRunLimit(i) +
                               " at level " + bidi.getRunLevel(i));
        }
        System.out.println(bidi + "\n");
        if (bidi.getRunCount() != 6) { 
            throw new Error("Bidi embedding processing failed");
        } else {
            System.out.println("test2() passed.\n");
        }
    }
}

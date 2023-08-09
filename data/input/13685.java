public class Test6931567 {
    public static void main(final String[] args) {
        booleanInvert(Integer.MAX_VALUE);
        booleanInvert(Integer.MAX_VALUE - 1);
    }
    private static void booleanInvert(final int max) {
        boolean test1 = false;
        boolean test2 = false;
        for (int i = 0; i < max; i++) {
            test1 = !test1;
        }
        for (int i = 0; i < max; i++) {
            test2 ^= true;
        }
        if (test1 != test2) {
            System.out.println("ERROR: Boolean invert\n\ttest1=" + test1
                    + "\n\ttest2=" + test2);
            System.exit(97);
        } else {
            System.out.println("Passed!");
        }
    }
}

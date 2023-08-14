public class Bug5047314 {
    private static Collator colLao = Collator.getInstance(new Locale("lo"));
    private static Collator colThai = Collator.getInstance(new Locale("th"));
    private static String[] textLao = {
        "\u0ec0", "\u0ec1", "\u0ec2", "\u0ec3", "\u0ec4"
    };
    private static String[] textThai = {
        "\u0e40", "\u0e41", "\u0e42", "\u0e43", "\u0e44"
    };
    public static void main(String[] args) {
        testLao1();
        testLao2();
        testThai1();
        testThai2();
    }
    private static void testLao1() {
        System.out.print("Test(Lao 1) .... ");
        for (int i = 0; i < textLao.length; i++) {
            colLao.compare(textLao[i], textLao[i]);
        }
        System.out.println("Passed.");
    }
    private static void testLao2() {
        System.out.print("Test(Lao 2) .... ");
        for (int i = 0; i < textLao.length; i++) {
            colLao.compare(textLao[i], textLao[i]);
        }
        System.out.println("Passed.");
    }
    private static void testThai1() {
        System.out.print("Test(Thai 1) .... ");
        for (int i = 0; i < textThai.length; i++) {
            colThai.compare(textThai[i], textThai[i]);
        }
        System.out.println("Passed.");
    }
    private static void testThai2() {
        System.out.print("Test(Thai 2) .... ");
        for (int i = 0; i < textThai.length; i++) {
            colThai.getCollationKey(textThai[i]);
        }
        System.out.println("Passed.");
    }
}

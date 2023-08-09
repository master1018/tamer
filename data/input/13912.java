public class SupplementaryJavaID1 {
    public static void main(String[] s) {
        int \ud801\udc00abc = 1;
        int \ud802\udc00abc = 2;
        int \ud801\udc01abc = 3;
        int def\ud801\udc00 = 4;
        int \ud801\udc00\ud834\udd7b = 5;
        if (\ud801\udc00abc != 1 ||
            \ud802\udc00abc != 2 ||
            \ud801\udc01abc != 3 ||
            def\ud801\udc00 != 4 ||
            \ud801\udc00\ud834\udd7b != 5) {
                throw new RuntimeException("test failed");
        }
    }
}

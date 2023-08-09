public class CompareIC {
    public static void main(String[] args) throws Exception {
        String test1 = "Tess";
        String test2 = "Test";
        String test3 = "Tesu";
        CompareIC comparer = new CompareIC();
        comparer.testTriplet(test1, test2, test3);
        test2 = test2.toUpperCase();
        comparer.testTriplet(test1, test2, test3);
        test2 = test2.toLowerCase();
        comparer.testTriplet(test1, test2, test3);
    }
    private void testTriplet(String one, String two, String three)
        throws Exception {
            if (one.compareToIgnoreCase(two) > 0)
                throw new RuntimeException("Comparison failure1");
            if (two.compareToIgnoreCase(three) > 0)
                throw new RuntimeException("Comparison failure2");
            if (three.compareToIgnoreCase(one) < 0)
                throw new RuntimeException("Comparison failure3");
    }
}

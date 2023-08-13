public class Bug6970930 {
    private static boolean err = false;
    public static void main(String[] args) {
        test1(null, null);
        test1("\"foo\"", null);
        test1(null, "\"bar\"");
        if (err) {
            throw new RuntimeException("Failed.");
        } else {
            System.out.println("Passed.");
        }
    }
    private static void test1(String s1, String s2) {
        RuleBasedCollator col = null;
        try {
            col = new RuleBasedCollator("< a < b");
        }
        catch (ParseException e) {
            err = true;
            System.err.println(e);
        }
        try {
            col.compare("foo", "bar"); 
            col.compare(s1, s2);
            err = true;
            System.err.println("No exception was thrown for compare(" +
                               s1 + ", " +  s2 + ").");
        }
        catch (NullPointerException e) {
            System.out.println("NPE was thrown as expected for compare(" +
                               s1 + ", " + s2 + ").");
        }
        catch (Exception e) {
            err = true;
            System.err.println("Unexpected exception was thrown for compare(" +
                               s1 + ", " + s2 + "): " + e);
        }
    }
}

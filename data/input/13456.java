public class Regex {
    static void ck(boolean x, boolean ans) throws Exception {
        if (x != ans)
            throw new Exception("Test failed");
    }
    static void ck(String x, String ans) throws Exception {
        if (!x.equals(ans))
            throw new Exception("Test failed");
    }
    static void ck(String[] x, String[] ans) throws Exception {
        if (x.length != ans.length)
            throw new Exception("Test failed");
        for (int i = 0; i < x.length; i++) {
            if (!x[i].equals(ans[i]))
                throw new Exception("Test failed");
        }
    }
    static void testLiteralReplacement() throws Exception {
        String data = "abcdefghi";
        String result = data.replace("def", "abc");
        if (!result.equals("abcabcghi"))
            throw new Exception("Test failed");
        data = "abc(def)?ghi";
        result = data.replace("(def)?", "abc");
        if (!result.equals("abcabcghi"))
            throw new Exception("Test failed");
        data = "abcdefghi";
        result = data.replace("def", "\\ab$c");
        if (!result.equals("abc\\ab$cghi"))
            throw new Exception("Test failed");
    }
    public static void main(String[] args) throws Exception {
        String foo = "boo:and:foo";
        ck(foo.matches("b+"), false);
        ck(foo.matches("o+"), false);
        ck(foo.matches("b..:and:f.*"), true);
        ck(foo.replaceAll("oo", "uu"), "buu:and:fuu");
        ck(foo.replaceAll("o+", "<$0>"), "b<oo>:and:f<oo>");
        ck(foo.replaceFirst("oo", "uu"), "buu:and:foo");
        ck(foo.replaceFirst("o+", "<$0>"), "b<oo>:and:foo");
        ck(foo.split(":"), new String[] { "boo", "and", "foo" });
        ck(foo.split("o"), new String[] { "b", "", ":and:f" });
        ck(foo.split(":", 2), new String[] { "boo", "and:foo" });
        ck(foo.split("o", -2), new String[] { "b", "", ":and:f", "", "" });
        testLiteralReplacement();
    }
}

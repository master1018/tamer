public class ResetPos {
    static void checkValue(String val, String checkVal) {
        System.out.println("Comparing \""+ val + "\" <----> \"" + checkVal +
                "\"");
        if (!val.equals(checkVal))
            throw new RuntimeException("Test failed");
    }
    public static void main(String[] argv) {
        StringTokenizer st1 = new StringTokenizer("ab", "b", true);
        checkValue("a", st1.nextToken("b"));
        st1.hasMoreTokens();
        checkValue("b", st1.nextToken(""));
        StringTokenizer st2 = new StringTokenizer("abcd efg", "abc", true);
        st2.hasMoreTokens();
        checkValue("a", st2.nextToken("bc"));
        st2.hasMoreTokens();
        checkValue("b", st2.nextToken());
        st2.hasMoreTokens();
        checkValue("cd", st2.nextToken(" ef"));
        st2.hasMoreTokens();
        checkValue(" ", st2.nextToken(" "));
        st2.hasMoreTokens();
        checkValue("ef", st2.nextToken("g"));
        st2.hasMoreTokens();
        checkValue("g", st2.nextToken("g"));
        StringTokenizer st3 = new StringTokenizer("this is,a interesting,sentence of small, words", ",");
        st3.hasMoreTokens();
        checkValue("this is", st3.nextToken()); 
        st3.hasMoreTokens();
        checkValue(",a", st3.nextToken(" "));   
        st3.hasMoreTokens();
        checkValue(" interesting", st3.nextToken(",")); 
        st3.hasMoreTokens();
        checkValue(",sentence", st3.nextToken(" ")); 
        st3.hasMoreTokens();
        checkValue(" of small", st3.nextToken(",")); 
        st3.hasMoreTokens();
        checkValue(" words", st3.nextToken()); 
    }
}

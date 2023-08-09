public class toStringTest {
    static void testString(int test, String expected) {
        if(!Modifier.toString(test).equals(expected))
            throw new RuntimeException(test +
                                          " yields incorrect toString result");
    }
    public static void main(String [] argv) {
        int allMods = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
            Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL |
            Modifier.TRANSIENT | Modifier.VOLATILE | Modifier.SYNCHRONIZED |
            Modifier.NATIVE | Modifier.STRICT | Modifier.INTERFACE;
        String allModsString = "public protected private abstract static " +
            "final transient volatile synchronized native strictfp interface";
        testString(0, "");
        testString(allMods, allModsString);
        testString(~0, allModsString);
    }
}

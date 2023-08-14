public class TestValueTag extends JavadocTester {
    private static final String BUG_ID = "4764045";
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "-tag",
            "todo", "pkg1", "pkg2"
        };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  \"Test 1 passes\""},
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_2_PASSES\">\"Test 2 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_3_PASSES\">\"Test 3 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_4_PASSES\">\"Test 4 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_5_PASSES\">\"Test 5 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class1.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_6_PASSES\">\"Test 6 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_7_PASSES\">\"Test 7 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_8_PASSES\">\"Test 8 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_9_PASSES\">\"Test 9 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_10_PASSES\">\"Test 10 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg1/Class1.html#TEST_11_PASSES\">\"Test 11 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg2/Class3.html#TEST_12_PASSES\">\"Test 12 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg2/Class3.html#TEST_13_PASSES\">\"Test 13 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg2/Class3.html#TEST_14_PASSES\">\"Test 14 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg2/Class3.html#TEST_15_PASSES\">\"Test 15 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "Class2.html",
            "Result:  <a href=\"../pkg2/Class3.html#TEST_16_PASSES\">\"Test 16 passes\"</a>"},
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "Result: <a href=\"../pkg2/Class3.html#TEST_17_PASSES\">\"Test 17 passes\"</a>"},
        {BUG_ID + FS + "pkg1" + FS + "CustomTagUsage.html",
            "<dt><span class=\"strong\">Todo:</span></dt>" + NL +
                "  <dd>the value of this constant is 55.</dd>"},
        {WARNING_OUTPUT,"warning - @value tag (which references nonConstant) " +
            "can only be used in constants."
        },
        {WARNING_OUTPUT,"warning - UnknownClass#unknownConstant (referenced by " +
            "@value tag) is an unknown reference."
        },
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestValueTag tester = new TestValueTag();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}

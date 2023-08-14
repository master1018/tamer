public class TestTypeParameters extends JavadocTester {
    private static final String BUG_ID = "4927167-4974929-7010344";
    private static final String[] ARGS1 = new String[]{
        "-d", BUG_ID, "-use", "-source", "1.5", "-sourcepath", SRC_DIR,
        "pkg"
    };
    private static final String[] ARGS2 = new String[]{
        "-d", BUG_ID, "-linksource", "-source", "1.5", "-sourcepath", SRC_DIR,
        "pkg"
    };
    private static final String[][] TEST1 = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<td class=\"colFirst\"><code>&lt;W extends java.lang.String,V extends " +
            "java.util.List&gt;&nbsp;<br>java.lang.Object</code></td>"
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<code>&lt;T&gt;&nbsp;java.lang.Object</code>"
        },
        {BUG_ID + FS + "pkg" + FS + "package-summary.html",
            "C</a>&lt;E extends <a href=\"../pkg/Parent.html\" " +
            "title=\"class in pkg\">Parent</a>&gt;"
        },
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "Foo4.html",
            "<a href=\"../../pkg/ClassUseTest3.html\" title=\"class in pkg\">" +
            "ClassUseTest3</a>&lt;T extends <a href=\"../../pkg/ParamTest2.html\" " +
            "title=\"class in pkg\">ParamTest2</a>&lt;java.util.List&lt;? extends " +
            "<a href=\"../../pkg/Foo4.html\" title=\"class in pkg\">Foo4</a>&gt;&gt;&gt;"
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a name=\"formatDetails(java.util.Collection, java.util.Collection)\">" + NL +
            "<!--   -->" + NL +
            "</a>"
        },
    };
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "pkg" + FS + "ClassUseTest3.html",
            "public class <a href=\"../src-html/pkg/ClassUseTest3.html#line.28\">" +
            "ClassUseTest3</a>&lt;T extends <a href=\"../pkg/ParamTest2.html\" " +
            "title=\"class in pkg\">ParamTest2</a>&lt;java.util.List&lt;? extends " +
            "<a href=\"../pkg/Foo4.html\" title=\"class in pkg\">Foo4</a>&gt;&gt;&gt;"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestTypeParameters tester = new TestTypeParameters();
        run(tester, ARGS1, TEST1, NEGATED_TEST);
        run(tester, ARGS2, TEST2, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}

public class TestHeadings extends JavadocTester {
    private static final String BUG_ID = "4905786-6259611";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-use", "-header", "Test Files",
        "pkg1", "pkg2"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Class</th>" + NL + "<th class=\"colLast\" scope=\"col\"" +
            ">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<h3>Methods inherited from class&nbsp;java.lang.Object</h3>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Package</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<th class=\"colOne\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "constant-values.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Modifier and Type</th>" + NL + "<th scope=\"col\">Constant Field</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Value</th>"
        },
        {BUG_ID + FS + "serialized-form.html",
            "<h2 title=\"Package\">Package&nbsp;pkg1</h2>"
        },
        {BUG_ID + FS + "serialized-form.html",
            "<h3>Class <a href=\"pkg1/C1.html\" title=\"class in pkg1\">" +
            "pkg1.C1</a> extends java.lang.Object implements Serializable</h3>"
        },
        {BUG_ID + FS + "serialized-form.html",
            "<h3>Serialized Fields</h3>"
        },
        {BUG_ID + FS + "overview-frame.html",
            "<h1 title=\"Test Files\" class=\"bar\">Test Files</h1>"
        },
        {BUG_ID + FS + "overview-frame.html",
            "<title>Overview List</title>"
        },
        {BUG_ID + FS + "overview-summary.html",
            "<title>Overview</title>"
        },
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestHeadings tester = new TestHeadings();
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

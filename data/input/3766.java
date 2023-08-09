public class EagerInterfaceCompletionTest {
    JavaCompiler javacTool;
    File testDir;
    HierarchyKind hierarchyKind;
    TestKind testKind;
    ActionKind actionKind;
    EagerInterfaceCompletionTest(JavaCompiler javacTool, File testDir,
            HierarchyKind hierarchyKind, TestKind testKind, ActionKind actionKind) {
        this.javacTool = javacTool;
        this.hierarchyKind = hierarchyKind;
        this.testDir = testDir;
        this.testKind = testKind;
        this.actionKind = actionKind;
    }
    void test() {
        testDir.mkdirs();
        compile(null, hierarchyKind.source);
        actionKind.doAction(this);
        DiagnosticChecker dc = new DiagnosticChecker();
        compile(dc, testKind.source);
        if (testKind.completionFailure(actionKind, hierarchyKind) != dc.errorFound) {
            if (dc.errorFound) {
                error("Unexpected completion failure" +
                      "\nhierarhcyKind " + hierarchyKind +
                      "\ntestKind " + testKind +
                      "\nactionKind " + actionKind);
            } else {
                error("Missing completion failure " +
                          "\nhierarhcyKind " + hierarchyKind +
                          "\ntestKind " + testKind +
                          "\nactionKind " + actionKind);
            }
        }
    }
    void compile(DiagnosticChecker dc, JavaSource... sources) {
        try {
            CompilationTask ct = javacTool.getTask(null, null, dc,
                    Arrays.asList("-d", testDir.getAbsolutePath(), "-cp", testDir.getAbsolutePath()),
                    null, Arrays.asList(sources));
            ct.call();
        }
        catch (Exception e) {
            e.printStackTrace();
            error("Internal compilation error");
        }
    }
    void removeClass(String classToRemoveStr) {
        File classToRemove = new File(testDir, classToRemoveStr);
        if (!classToRemove.exists()) {
            error("Expected file " + classToRemove + " does not exists in folder " + testDir);
        }
        classToRemove.delete();
    };
    void error(String msg) {
        System.err.println(msg);
        nerrors++;
    }
    class DiagnosticChecker implements DiagnosticListener<JavaFileObject> {
        boolean errorFound = false;
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            errorFound = true;
        }
    }
    enum HierarchyKind {
        INTERFACE("interface A { boolean f = false; void m(); }\n" +
                  "class B implements A { public void m() {} }"),
        CLASS("class A { boolean f; void m() {} }\n" +
              "class B extends A { void m() {} }"),
        ABSTRACT_CLASS("abstract class A { boolean f; abstract void m(); }\n" +
                       "class B extends A { void m() {} }");
        JavaSource source;
        private HierarchyKind(String code) {
            this.source = new JavaSource("Test1.java", code);
        }
    }
    enum ActionKind {
        REMOVE_A("A.class"),
        REMOVE_B("B.class");
        String classFile;
        private ActionKind(String classFile) {
            this.classFile = classFile;
        }
        void doAction(EagerInterfaceCompletionTest test) {
            test.removeClass(classFile);
        };
    }
    enum TestKind {
        ACCESS_ONLY("class C { B b; }"),
        SUPER("class C extends B {}"),
        METHOD("class C { void test(B b) { b.m(); } }"),
        FIELD("class C { void test(B b) { boolean b2 = b.f; } }"),
        CONSTR("class C { void test() { new B(); } }");
        JavaSource source;
        private TestKind(final String code) {
            this.source = new JavaSource("Test2.java", code);
        }
        boolean completionFailure(ActionKind ak, HierarchyKind hk) {
            switch (this) {
                case ACCESS_ONLY:
                case CONSTR: return ak == ActionKind.REMOVE_B;
                case FIELD:
                case SUPER: return true;
                case METHOD: return hk != HierarchyKind.INTERFACE || ak == ActionKind.REMOVE_B;
                default: throw new AssertionError("Unexpected test kind " + this);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        String SCRATCH_DIR = System.getProperty("user.dir");
        JavaCompiler javacTool = ToolProvider.getSystemJavaCompiler();
        int n = 0;
        for (HierarchyKind hierarchyKind : HierarchyKind.values()) {
            for (TestKind testKind : TestKind.values()) {
                for (ActionKind actionKind : ActionKind.values()) {
                    File testDir = new File(SCRATCH_DIR, "test"+n);
                    new EagerInterfaceCompletionTest(javacTool, testDir, hierarchyKind, testKind, actionKind).test();
                    n++;
                }
            }
        }
        if (nerrors > 0) {
            throw new AssertionError("Some errors have been detected");
        }
    }
    static class JavaSource extends SimpleJavaFileObject {
        String source;
        public JavaSource(String filename, String source) {
            super(URI.create("myfo:/" + filename), JavaFileObject.Kind.SOURCE);
            this.source = source;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
    static int nerrors = 0;
}

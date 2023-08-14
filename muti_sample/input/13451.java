public class FakeTestDriver  {
    private String[] fTestList = {
        "javafake.lang.instrument.AddTransformerTest",
        "javafake.lang.instrument.AppendToBootstrapClassPathTest",
        "javafake.lang.instrument.AppendToClassPathTest",
        "javafake.lang.instrument.GetAllLoadedClassesTest",
        "javafake.lang.instrument.GetInitiatedClassesTest",
        "javafake.lang.instrument.GetObjectSizeTest",
        "javafake.lang.instrument.NoTransformerAddedTest",
        "javafake.lang.instrument.NullTransformerAddTest",
        "javafake.lang.instrument.RedefineClassesTests",
        "javafake.lang.instrument.RemoveAbsentTransformerTest",
        "javafake.lang.instrument.RemoveTransformerTest",
        "javafake.lang.instrument.SingleTransformerTest",
        "javafake.lang.instrument.TransformerManagementThreadAddTests",
        "javafake.lang.instrument.TransformerManagementThreadRemoveTests",
        "javafake.lang.instrument.TransformMethodTest",
    };
    public static void
    main(String[] args) {
        (new FakeTestDriver()).runSuppliedTests(args);
    }
    private
    FakeTestDriver() {
    }
    private void
    runAllTests() {
        runSuppliedTests(fTestList);
    }
    private void
    runSuppliedTests(String[] classnames) {
        for (int x = 0; x < classnames.length; x++ ) {
            loadAndRunOneTest(classnames[x]);
        }
    }
    private void
    loadAndRunOneTest(String classname) {
        log("trying to run: " + classname);
        Class testclass = loadOneTest(classname);
        if ( testclass != null ) {
            boolean result = runOneTest(testclass);
            if ( result ) {
                log(classname + " SUCCEEDED");
            }
            else {
                log(classname + " FAILED");
            }
        }
        else {
            log(classname + " could not be loaded");
        }
    }
    private Class
    loadOneTest(String classname) {
        Class result = null;
        try {
            result = Class.forName(classname);
        }
        catch (Throwable t) {
            t.printStackTrace();
            result = null;
        }
        return result;
    }
    private boolean
    runOneTest(Class testclass) {
        Method mainMethod = null;
        try {
            String[]    forType = new String[0];
            mainMethod = testclass.getMethod("main",
                                                    new Class[] {
                                                        forType.getClass()
                                                     });
        }
        catch (Throwable t) {
            log(testclass.getName() + " is malformed");
            t.printStackTrace();
            return false;
        }
        try {
            mainMethod.invoke(null, new Object[] {new String[] {testclass.getName()}});
            return true;
        }
        catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    private void
    log(String m) {
        System.out.println(m);
    }
}

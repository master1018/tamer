public abstract class AInstrumentationTestCase extends ATestCaseScaffold {
    protected Instrumentation   fInst;
    protected
    AInstrumentationTestCase(String name) {
        super(name);
    }
    protected void
    setUp()
        throws Exception {
        fInst = InstrumentationHandoff.getInstrumentationOrThrow();
    }
    protected void
    tearDown()
        throws Exception {
        fInst = null;
    }
}

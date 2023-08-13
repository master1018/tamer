public class FromShutdownHook
    extends ASimpleInstrumentationTestCase
{
    public FromShutdownHook(String name) {
        super(name);
    }
    public static void main(String args[] ) throws Throwable {
        FromShutdownHook fsh = new FromShutdownHook(args[0]);
        fsh.runTest();
    }
    Instrumentation ins;
    protected final void doRunTest() {
        ins = fInst;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.err.println(ins.getAllLoadedClasses().length +
                        " classes loaded.");
            }
        });
    }
}

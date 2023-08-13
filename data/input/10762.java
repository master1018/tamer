public class StressGetObjectSizeApp
    extends ASimpleInstrumentationTestCase
{
    public StressGetObjectSizeApp(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new StressGetObjectSizeApp(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        stressGetObjectSize();
    }
    public void stressGetObjectSize() {
        System.out.println("main: an object size=" +
            fInst.getObjectSize(new Object()));
        RoundAndRound[] threads = new RoundAndRound[10];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new RoundAndRound(fInst);
            threads[i].start();
        }
        try {
            Thread.sleep(500); 
        } catch (InterruptedException ie) {
        }
        System.out.println("stressGetObjectSize: returning");
        return;
    }
    private static class RoundAndRound extends Thread {
        private final Instrumentation inst;
        private final Object anObject;
        public RoundAndRound(Instrumentation inst) {
            this.inst = inst;
            this.anObject = new Object();
            setDaemon(true);
        }
        public void run() {
            long sum = 0;
            while (true) {
              sum += inst.getObjectSize(anObject);
            }
        }
    }
}

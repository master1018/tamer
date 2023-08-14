class AnyDebuggeeTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        try {
            javax.swing.UIManager.setLookAndFeel( javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch( Throwable exc) {
        }
        JFrame f = new JFrame("JFrame");
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ee) {
        }
        System.out.println("Goodbye from NestedClassesTarg!");
    }
}
public class AnyDebuggeeTest extends TestScaffold {
    static String targetName = "AnyDebuggeeTarg";
    ReferenceType targetClass;
    ThreadReference mainThread;
    AnyDebuggeeTest(String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        for (int ii = 0; ii < args.length; ii ++) {
            if (args[ii].startsWith("@@")) {
                targetName = args[ii] = args[ii].substring(2);
            }
        }
        new AnyDebuggeeTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe;
        bpe = startToMain(targetName);
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        resumeForMsecs(20000);
        List<ReferenceType> allClasses = vm().allClasses();
        System.out.println( allClasses.size() + " classes");
        int size = 0;
        long start = System.currentTimeMillis();
        for(ReferenceType rt: allClasses) {
            if (rt instanceof ClassType) {
            List<ReferenceType> nested = rt.nestedTypes();
            int sz = nested.size();
            size += sz;
        }
        }
        long end = System.currentTimeMillis();
        System.out.println(size + " nested types took " + (end - start) + " ms");
        size = 0;
        start = System.currentTimeMillis();
        for(ReferenceType rt: allClasses) {
            if (rt instanceof ClassType) {
                List<ClassType> subs = ((ClassType)rt).subclasses();
                int sz = subs.size();
                size += sz;
            }
        }
        end = System.currentTimeMillis();
        System.out.println(size + " subclasses took " + (end - start) + " ms");
        if (!testFailed) {
            println("AnyDebuggeeTest: passed");
        } else {
            throw new Exception("AnyDebuggeeTest: failed");
        }
    }
}

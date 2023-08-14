class ClassesByName2Targ {
    static void bkpt() {
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        try {
            Thread zero = new Thread ("ZERO") {
                    public void run () {
                        System.setProperty("java.awt.headless", "true");
                        java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
                    }
                };
            Thread one = new Thread ("ONE") {
                    public void run () {
                        try {
                            java.security.KeyPairGenerator keyGen =
                                java.security.KeyPairGenerator.getInstance("DSA", "SUN");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
            Thread two = new Thread ("TWO") {
                    public void run () {
                        javax.rmi.CORBA.Util.getCodebase(this.getClass());
                    }
                };
            two.start();
            one.start();
            zero.start();
            try {
                zero.join();
                System.out.println("zero joined");
                one.join();
                System.out.println("one joined");
                two.join();
                System.out.println("two joined");
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bkpt();
        System.out.println("Goodbye from ClassesByName2Targ!");
    }
}
public class ClassesByName2Test extends TestScaffold {
    volatile boolean stop = false;
    ClassesByName2Test (String args[]) {
        super(args);
    }
    public void breakpointReached(BreakpointEvent event) {
        System.out.println("Got BreakpointEvent: " + event);
        stop = true;
    }
    public void eventSetComplete(EventSet set) {
    }
    public static void main(String[] args)      throws Exception {
        new ClassesByName2Test(args).startTests();
    }
    void breakpointAtMethod(ReferenceType ref, String methodName)
                                           throws Exception {
        List meths = ref.methodsByName(methodName);
        if (meths.size() != 1) {
            throw new Exception("test error: should be one " +
                                methodName);
        }
        Method meth = (Method)meths.get(0);
        BreakpointRequest bkptReq = vm().eventRequestManager().
            createBreakpointRequest(meth.location());
        bkptReq.enable();
        try {
            addListener (this);
        } catch (Exception ex){
            ex.printStackTrace();
            failure("failure: Could not add listener");
            throw new Exception("ClassesByname2Test: failed");
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ClassesByName2Targ");
        breakpointAtMethod(bpe.location().declaringType(), "bkpt");
        vm().resume();
        for (int i = 0; i < 150 && !stop; i++) {
            List all = vm().allClasses();
            System.out.println("\n++++ Lookup number: " + i + ".  allClasses() returned " +
                               all.size() + " classes.");
            for (Iterator it = all.iterator(); it.hasNext(); ) {
                ReferenceType cls = (ReferenceType)it.next();
                String name = cls.name();
                List found = vm().classesByName(name);
                if (found.contains(cls)) {
                } else {
                    System.out.println("CLASS NOT FOUND: " + name);
                    throw new Exception("CLASS NOT FOUND (by classesByName): " +
                                        name);
                }
            }
        }
        vm().exit(0);
        if (!testFailed) {
            println("ClassesByName2Test: passed");
        } else {
            throw new Exception("ClassesByName2Test: failed");
        }
    }
}

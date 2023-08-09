public class UnpreparedByName extends JDIScaffold {
    final String[] args;
    public static void main(String args[]) throws Exception {
        new UnpreparedByName(args).startTests();
    }
    UnpreparedByName(String args[]) throws Exception {
        super();
        this.args = args;
    }
    protected void runTests() throws Exception {
        connect(args);
        waitForVMStart();
        resumeTo("InnerTarg", "go", "()V");
        List classes = vm().classesByName("InnerTarg$TheInner");
        if (classes.size() == 0) {
            System.out.println("PASS: InnerTarg$TheInner not returned");
        } else {
            ReferenceType cls = (ReferenceType)classes.get(0);
            boolean preped = cls.isPrepared();
            if (!preped) {
                System.err.println("Inner class not prepared: " + cls);
            }
            cls.methods();  
            if (!preped) {
                throw new Exception("Inner class not prepared: " + cls);
            }
        }
        resumeToVMDeath();
    }
}

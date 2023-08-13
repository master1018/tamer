public class UnpreparedClasses extends JDIScaffold {
    final String[] args;
    public static void main(String args[]) throws Exception {
        new UnpreparedClasses(args).startTests();
    }
    UnpreparedClasses(String args[]) throws Exception {
        super();
        this.args = args;
    }
    protected void runTests() throws Exception {
        connect(args);
        waitForVMStart();
        resumeTo("InnerTarg", "go", "()V");
        List all = vm().allClasses();
        for (Iterator it = all.iterator(); it.hasNext(); ) {
            ReferenceType cls = (ReferenceType)it.next();
            boolean preped = cls.isPrepared() || (cls instanceof ArrayReference);
            if (!preped) {
                System.err.println("Class not prepared: " + cls);
            }
            cls.methods();  
            if (!preped) {
                throw new Exception("Class not prepared: " + cls);
            }
        }
        resumeToVMDeath();
    }
}

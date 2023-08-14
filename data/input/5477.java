public class ClassesByName extends JDIScaffold {
    final String[] args;
    public static void main(String args[]) throws Exception {
        new ClassesByName(args).startTests();
    }
    ClassesByName(String args[]) throws Exception {
        super();
        this.args = args;
    }
    protected void runTests() throws Exception {
        connect(args);
        waitForVMStart();
        List all = vm().allClasses();
        for (Iterator it = all.iterator(); it.hasNext(); ) {
            ReferenceType cls = (ReferenceType)it.next();
            String name = cls.name();
            List found = vm().classesByName(name);
            if (found.contains(cls)) {
                System.out.println("Found class: " + name);
            } else {
                System.out.println("CLASS NOT FOUND: " + name);
                throw new Exception("CLASS NOT FOUND (by classesByName): " + name);
            }
        }
        resumeToVMDeath();
    }
}

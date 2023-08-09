public class AllLineLocations extends JDIScaffold {
    final String[] args;
    public static void main(String args[]) throws Exception {
        new AllLineLocations(args).startTests();
    }
    AllLineLocations(String args[]) {
        super();
        this.args = args;
    }
    protected void runTests() throws Exception {
        connect(args);
        waitForVMStart();
        BreakpointEvent bp = resumeTo("RefTypes", "loadClasses", "()V");
        stepOut(bp.thread());
        ReferenceType rt = findReferenceType("AllAbstract");
        if (rt == null) {
            throw new Exception("AllAbstract: not loaded");
        }
        List list = rt.allLineLocations();
        if (list.size() != 1) {
            throw new Exception("AllAbstract: incorrect number of line locations");
        }
        if (rt.locationsOfLine(5000).size() != 0) {
            throw new Exception("AllAbstract: incorrect locationsOfLine");
        }
        Method method = findMethod(rt, "<init>", "()V");
        if (method == null) {
            throw new Exception("AllAbstract.<init> not found");
        }
        List list2 = method.allLineLocations();
        if (!list2.equals(list)) {
            throw new Exception("AllAbstract: line locations in wrong method");
        }
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("AllAbstract: incorrect locationsOfLine");
        }
        System.out.println("AllAbstract: passed");
        rt = findReferenceType("AllNative");
        if (rt == null) {
            throw new Exception("AllNative: not loaded");
        }
        list = rt.allLineLocations();
        if (list.size() != 1) {
            throw new Exception("AllNative: incorrect number of line locations");
        }
        if (rt.locationsOfLine(5000).size() != 0) {
            throw new Exception("AllNative: incorrect locationsOfLine");
        }
        method = findMethod(rt, "<init>", "()V");
        if (method == null) {
            throw new Exception("AllNative.<init> not found");
        }
        list2 = method.allLineLocations();
        if (!list2.equals(list)) {
            throw new Exception("AllNative: line locations in wrong method");
        }
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("AllNative: incorrect locationsOfLine");
        }
        System.out.println("AllNative: passed");
        rt = findReferenceType("Interface");
        if (rt == null) {
            throw new Exception("Interface: not loaded");
        }
        list = rt.allLineLocations();
        if (list.size() != 0) {
            throw new Exception("Interface: locations reported for abstract methods");
        }
        System.out.println("Interface: passed");
        rt = findReferenceType("Abstract");
        if (rt == null) {
            throw new Exception("Abstract: not loaded");
        }
        list = rt.allLineLocations();
        if (list.size() != 5) {
            throw new Exception("Abstract: incorrect number of line locations");
        }
        method = findMethod(rt, "b", "()V");
        if (method == null) {
            throw new Exception("Abstract.b not found");
        }
        list2 = method.allLineLocations();
        list.removeAll(list2);
        if ((list.size() != 1) ||
            !(((Location)list.get(0)).method().name().equals("<init>"))) {
            throw new Exception("Abstract: line locations in wrong method");
        }
        if (method.locationsOfLine(20).size() != 1) {
            throw new Exception("Abstract method: incorrect locationsOfLine");
        }
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("Abstract method: incorrect locationsOfLine");
        }
        method = findMethod(rt, "a", "()V");
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("Abstract method: incorrect locationsOfLine");
        }
        System.out.println("Abstract: passed");
        rt = findReferenceType("Native");
        if (rt == null) {
            throw new Exception("Native: not loaded");
        }
        list = rt.allLineLocations();
        if (list.size() != 5) {
            throw new Exception("Native: incorrect number of line locations");
        }
        if (rt.locationsOfLine(5000).size() != 0) {
            throw new Exception("Native: incorrect locationsOfLine");
        }
        method = findMethod(rt, "b", "()V");
        if (method == null) {
            throw new Exception("Native.b not found");
        }
        list2 = method.allLineLocations();
        list.removeAll(list2);
        if ((list.size() != 1) ||
            !(((Location)list.get(0)).method().name().equals("<init>"))) {
            throw new Exception("Native: line locations in wrong method");
        }
        if (method.locationsOfLine(30).size() != 1) {
            throw new Exception("Native method: incorrect locationsOfLine");
        }
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("Native method: incorrect locationsOfLine");
        }
        method = findMethod(rt, "a", "()V");
        if (method.locationsOfLine(5000).size() != 0) {
            throw new Exception("Native method: incorrect locationsOfLine");
        }
        System.out.println("Native: passed");
        rt = findReferenceType("AbstractAndNative");
        if (rt == null) {
            throw new Exception("AbstractAndNative: not loaded");
        }
        list = rt.allLineLocations();
        if (list.size() != 5) {
            throw new Exception("AbstractAndNative: incorrect number of line locations");
        }
        if (rt.locationsOfLine(5000).size() != 0) {
            throw new Exception("AbstractAndNative: incorrect locationsOfLine");
        }
        method = findMethod(rt, "c", "()V");
        if (method == null) {
            throw new Exception("AbstractAndNative.c not found");
        }
        list2 = method.allLineLocations();
        list.removeAll(list2);
        if ((list.size() != 1) ||
            !(((Location)list.get(0)).method().name().equals("<init>"))) {
            throw new Exception("AbstractAndNative: line locations in wrong method");
        }
        System.out.println("AbstractAndNative: passed");
        resumeToVMDeath();
    }
}

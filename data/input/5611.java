class ClassLoaderClassesTarg {
    static int[] intArray = new int[10];
    static {
        intArray[1] = 99;
    }
    public static void main(String[] args){
        System.out.println("Goodbye from ClassLoaderClassesTarg!");
    }
}
public class ClassLoaderClassesTest extends TestScaffold {
    ReferenceType targetClass;
    ClassLoaderClassesTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new ClassLoaderClassesTest(args).startTests();
    }
    boolean findClass(String className) throws Exception {
        ClassLoaderReference cl = targetClass.classLoader();
        Iterator vci = cl.visibleClasses().iterator();
        while (vci.hasNext()) {
            ReferenceType rt = (ReferenceType)vci.next();
            println(rt.name() + " - " + rt.classLoader());
            if (rt.name().equals(className)) {
                return true;
            }
        }
        return false;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("ClassLoaderClassesTarg");
        targetClass = bpe.location().declaringType();
        if (findClass("int[]")) {
            println("int[] found");
        } else {
            failure("failed - int[] not found");
        }
        Field arrayField = targetClass.fieldByName("intArray");
        ArrayType arrayType = (ArrayType)arrayField.type();
        println("Type for intArray is " + arrayType);
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("ClassLoaderClassesTest: passed");
        } else {
            throw new Exception("ClassLoaderClassesTest: failed");
        }
    }
}

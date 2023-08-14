class ClsFoo implements IfcFoo {
}
class NewInstanceTarg {
    static ClsFoo aFoo = new ClsFoo();
    static Object[] objArray = new Object[10];
    static ClsFoo[] clsArray = new ClsFoo[10];
    static IfcFoo[] ifc0Array = new IfcFoo[10];
    static IfcFoo[] ifcArray = {aFoo, aFoo};
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from NewInstanceTarg!");
    }
}
public class NewInstanceTest extends TestScaffold {
    ReferenceType targetClass;
    NewInstanceTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new NewInstanceTest(args).startTests();
    }
    void makeArray(String fieldName) throws Exception {
        println("Making array for field: " + fieldName);
        Field arrayField = targetClass.fieldByName(fieldName);
        ArrayType arrayType = (ArrayType)arrayField.type();
        println("Type for " + fieldName + " is " + arrayType);
        ArrayReference arrayReference = arrayType.newInstance(20);
        println("Passed subtest: " + fieldName);
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("NewInstanceTarg");
        targetClass = bpe.location().declaringType();
        makeArray("objArray");
        makeArray("clsArray");
        makeArray("ifc0Array");
        makeArray("ifcArray");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("NewInstanceTest: passed");
        } else {
            throw new Exception("NewInstanceTest: failed");
        }
    }
}

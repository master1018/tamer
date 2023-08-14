abstract class AbstractNoLocInfoTarg implements InterfaceNoLocInfoTarg {
    protected int fld;
    AbstractNoLocInfoTarg() {
        fld = 1000;
    }
    public abstract int instanceMeth();
    public int instanceMeth1() {
        fld = 999;
        fld = instanceMeth();
        return 0;
    }
}
class NoLocInfoTarg extends AbstractNoLocInfoTarg {
    public static void main(String[] args){
        System.out.println("A number is: " + new NoLocInfoTarg().instanceMeth());
    }
    public static int staticMeth() {
        int i = 2;
        return i;
    }
    public int instanceMeth() {
        int i = 0;
        i++;
        return i + staticMeth();
    }
    private void voidInstanceMeth() {}
    static native int staticNativeMeth();
    native boolean instanceNativeMeth();
}
public class NoLocInfoTest extends TestScaffold {
    final String[] args;
    NoLocInfoTest (String args[]) {
        super(args);
        this.args = args;
    }
    public static void main(String[] args)      throws Exception {
        new NoLocInfoTest(args).startTests();
    }
    Method getMethod(String className, String methodName) {
        List refs = vm().classesByName(className);
        if (refs.size() != 1) {
            failure("Failure: " + refs.size() +
                    " ReferenceTypes named: " + className);
            return null;
        }
        ReferenceType refType = (ReferenceType)refs.get(0);
        List meths = refType.methodsByName(methodName);
        if (meths.size() != 1) {
            failure("Failure: " + meths.size() +
                    " methods named: " + methodName);
            return null;
        }
        return (Method)meths.get(0);
    }
    void checkLineNumberTable(String className, String methodName) {
        println("GetLineNumberTable for method: " + className + "." + methodName);
        Method method = getMethod(className, methodName);
        try {
            List locations = method.allLineLocations();
            failure("Failure: com.sun.jdi.AbsentInformationException was expected; " +
                    "LineNumberTable.size() = " + locations.size());
        }
        catch (com.sun.jdi.AbsentInformationException ex) {
            println("Success: com.sun.jdi.AbsentInformationException thrown as expected");
        }
        println("");
    }
    void checkEmptyLineNumberTable(String className, String methodName) {
        println("GetLineNumberTable for abstract/native method: " +
                 className + "." + methodName);
        Method method = getMethod(className, methodName);
        try {
            int size = method.allLineLocations().size();
            if (size == 0) {
               println("Succes: LineNumberTable.size() == " + size + " as expected");
            } else {
               failure("Failure: LineNumberTable.size()==" + size + ", but ZERO was expected");
            }
        }
        catch (com.sun.jdi.AbsentInformationException ex) {
            failure("Failure: com.sun.jdi.AbsentInformationException was not expected; ");
        }
        println("");
    }
    protected void runTests() throws Exception {
        startToMain("NoLocInfoTarg");
        println("\n Abstract Methods:");
        checkEmptyLineNumberTable("InterfaceNoLocInfoTarg", "instanceMeth");
        checkEmptyLineNumberTable("InterfaceNoLocInfoTarg", "instanceMeth1");
        checkEmptyLineNumberTable("AbstractNoLocInfoTarg",  "instanceMeth");
        println("\n Native Methods:");
        checkEmptyLineNumberTable("NoLocInfoTarg", "staticNativeMeth");
        checkEmptyLineNumberTable("NoLocInfoTarg", "instanceNativeMeth");
        println("\n Non-Abstract Methods of Abstract class:");
        checkLineNumberTable("AbstractNoLocInfoTarg", "<init>");
        checkLineNumberTable("AbstractNoLocInfoTarg", "instanceMeth1");
        println("\n Methods of Non-Abstract class:");
        checkLineNumberTable("NoLocInfoTarg", "<init>"); 
        checkLineNumberTable("NoLocInfoTarg", "main");
        checkLineNumberTable("NoLocInfoTarg", "instanceMeth");
        checkLineNumberTable("NoLocInfoTarg", "instanceMeth1"); 
        checkLineNumberTable("NoLocInfoTarg", "voidInstanceMeth");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("NoLocInfoTest: passed");
        } else {
            throw new Exception("NoLocInfoTest: failed");
        }
    }
}

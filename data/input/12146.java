class LocalVariableEqualTarg {
    public static void main(String[] args){
        int intVar = 10;
        System.out.println("LocalVariableEqualTarg: Started");
        intVar = staticMeth(intVar);
        System.out.println("LocalVariableEqualTarg: Finished");
    }
    public static int staticMeth(int intArg) {
        System.out.println("staticMeth: Started");
        int result;
        {
             { boolean bool_1 = false;
               intArg++;
               {
                 { byte byte_2 = 2;
                   intArg++;
                 }
                 byte byte_1 = 1;
                 intArg++;
               }
             }
             boolean bool_2 = true;
             intArg++;
        }
        {
             {
               {
                 { char   char_1 = '1';
                   intArg++;
                 }
                 short  short_1 = 1;
                 intArg++;
               }
             }
             { short  short_2 = 2;
               intArg++;
             }
             char   char_2 = '2';
             intArg++;
        }
        {
             { int int_1 = 1;
               intArg++;
             }
             long long_1 = 1;
             intArg++;
        }
        {
             { float  float_1 = 1;
               intArg++;
             }
             double double_2 = 2;
             intArg++;
        }
        {
             { String string_1 = "1";
               intArg++;
             }
             Object obj_2 = new Object();
             intArg++;
        }
        result = 10;
        System.out.println("staticMeth: Finished");
        return result;
    }
}
public class LocalVariableEqual extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    LocalVariableEqual (String args[]) {
        super(args);
    }
    public static void main(String[] args) throws Exception {
        new LocalVariableEqual(args).startTests();
    }
    Method getMethod(String className, String methodName) {
        List refs = vm().classesByName(className);
        if (refs.size() != 1) {
            failure("Test failure: " + refs.size() +
                    " ReferenceTypes named: " + className);
            return null;
        }
        ReferenceType refType = (ReferenceType)refs.get(0);
        List meths = refType.methodsByName(methodName);
        if (meths.size() != 1) {
            failure("Test failure: " + meths.size() +
                    " methods named: " + methodName);
            return null;
        }
        return (Method)meths.get(0);
    }
    void printVariable(LocalVariable lv, int index) throws Exception {
        if (lv == null) {
            println(" Var  name: null");
            return;
        }
        String tyname = lv.typeName();
        println(" Var: " + lv.name() + ", index: " + index + ", type: " + tyname +
                ", Signature: " + lv.type().signature());
    }
    void compareTwoEqualVars(LocalVariable lv1, LocalVariable lv2) {
        if (lv1.equals(lv2)) {
            println(" Success: equality of local vars detected");
        } else {
            failure(" Failure: equality of local vars is NOT detected");
        }
        if (lv1.hashCode() == lv2.hashCode()) {
            println(" Success: hashCode's of equal local vars are equal");
        } else {
            failure(" Failure: hashCode's of equal local vars differ");
        }
        if (lv1.compareTo(lv2) == 0) {
            println(" Success: compareTo() is correct for equal local vars");
        } else {
            failure(" Failure: compareTo() is NOT correct for equal local vars");
        }
    }
    void compareTwoDifferentVars(LocalVariable lv1, LocalVariable lv2) {
        if (!lv1.equals(lv2)) {
            println(" Success: difference of local vars detected");
        } else {
            failure(" Failure: difference of local vars is NOT detected");
        }
        if (lv1.hashCode() != lv2.hashCode()) {
            println(" Success: hashCode's of different local vars differ");
        } else {
            failure(" Failure: hashCode's of different local vars are equal");
        }
        if (lv1.compareTo(lv2) != 0) {
            println(" Success: compareTo() is correct for different local vars");
        } else {
            failure(" Failure: compareTo() is NOT correct for different local vars");
        }
    }
    void compareAllVariables(String className, String methodName) throws Exception {
        println("compareAllVariables for method: " + className + "." + methodName);
        Method method = getMethod(className, methodName);
        List localVars;
        try {
            localVars = method.variables();
            println("\n Success: got a list of all method variables: " + methodName);
        }
        catch (com.sun.jdi.AbsentInformationException ex) {
            failure("\n AbsentInformationException has been thrown");
            return;
        }
        int index1 = 0;
        for (Iterator it1 = localVars.iterator(); it1.hasNext(); index1++) {
            LocalVariable lv1 = (LocalVariable) it1.next();
            int index2 = 0;
            for (Iterator it2 = localVars.iterator(); it2.hasNext(); index2++) {
                LocalVariable lv2 = (LocalVariable) it2.next();
                println("\n Two variables:");
                printVariable(lv1, index1);
                printVariable(lv2, index2);
                println("");
                if (index1 == index2) {
                    compareTwoEqualVars(lv1, lv2);
                } else {
                    compareTwoDifferentVars(lv1, lv2);
                }
            }
        }
        println("");
        return;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("LocalVariableEqualTarg");
        println("startToMain(LocalVariableEqualTarg)");
        compareAllVariables("LocalVariableEqualTarg", "staticMeth");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("\nLocalVariableEqual: passed");
        } else {
            throw new Exception("\nLocalVariableEqual: FAILED");
        }
    }
}

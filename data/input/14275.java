class GetSetLocalTarg {
    public static void main(String[] args){
        int intVar = 10;
        System.out.println("GetSetLocalTarg: Started");
        intVar = staticMeth(intVar);
        System.out.println("GetSetLocalTarg: Finished");
    }
    public static int staticMeth(int intArg) {
        System.out.println("staticMeth: Started");
        int result;
        {
             { boolean bool_1 = false;
               intArg++;
             }
             boolean bool_2 = true;
             intArg++;
        }
        {
             { byte byte_1 = 1;
               intArg++;
             }
             byte byte_2 = 2;
             intArg++;
        }
        {
             { char   char_1 = '1';
               intArg++;
             }
             char   char_2 = '2';
             intArg++;
        }
        {
             { short  short_1 = 1;
               intArg++;
             }
             short  short_2 = 2;
             intArg++;
        }
        {
             { int    int_1 = 1;
               intArg++;
             }
             int int_2 = 2;
             intArg++;
        }
        {
             { long long_1 = 1;
               intArg++;
             }
             long long_2 = 2;
             intArg++;
        }
        {
             { float  float_1 = 1;
               intArg++;
             }
             float float_2 = 2;
             intArg++;
        }
        {
             { double double_1 = 1;
               intArg++;
             }
             double double_2 = 2;
             intArg++;
        }
        {
             { String string_1 = "1";
               intArg++;
             }
             String string_2 = "2";
             intArg++;
        }
        {
             { Object obj_1 = new Object();
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
public class GetSetLocalTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    GetSetLocalTest (String args[]) {
        super(args);
    }
    public static void main(String[] args) throws Exception {
        new GetSetLocalTest(args).startTests();
    }
    Method getMethod(String className, String methodName) {
        List refs = vm().classesByName(className);
        if (refs.size() != 1) {
            failure(" Failure: " + refs.size() +
                    " ReferenceTypes named: " + className);
            return null;
        }
        ReferenceType refType = (ReferenceType)refs.get(0);
        List meths = refType.methodsByName(methodName);
        if (meths.size() != 1) {
            failure(" Failure: " + meths.size() +
                    " methods named: " + methodName);
            return null;
        }
        return (Method)meths.get(0);
    }
    List printAllVariables(String className, String methodName) throws Exception {
        println("printAllVariables for method: " + className + "." + methodName);
        Method method = getMethod(className, methodName);
        List localVars;
        try {
            localVars = method.variables();
            println(" Success: got a list of all method variables: " + methodName);
        }
        catch (com.sun.jdi.AbsentInformationException ex) {
            failure(" Failure: AbsentInformationException has been thrown");
            return null;
        }
        int index = 0;
        for (Iterator it = localVars.iterator(); it.hasNext();) {
            LocalVariable lv = (LocalVariable) it.next();
            printOneVariable(lv, index++);
        }
        println("");
        return localVars;
    }
    void checkGetSetAllVariables(List localVars, StackFrame frame) throws Exception {
        println("\n checkGetSetAllVariables for method at particular frame location: ");
        int index = 0;
        for (Iterator it = localVars.iterator(); it.hasNext();) {
            LocalVariable lv = (LocalVariable) it.next();
            String lv_name = lv.name();
            print(" Variable " + lv_name);
            try {
                Value val = frame.getValue(lv);
                frame.setValue(lv, val);
                println(" has been get/set");
                if (lv_name.compareTo("intArg") != 0 &&
                    lv_name.compareTo("obj_2")  != 0) {
                    failure(" Failure: AbsentInformationException is expected");
                }
            } catch (java.lang.IllegalArgumentException ex) {
                println(" is not valid");
                if (lv_name.compareTo("intArg") == 0 &&
                    lv_name.compareTo("obj_2")  == 0) {
                    failure(" Failure: AbsentInformationException was not expected");
                }
            }
        }
        println("");
    }
    void printOneVariable(LocalVariable lv, int index) throws Exception {
        String tyname = lv.typeName();
        println("");
        println(" Var  name: " + lv.name());
        println(" Var  type: " + tyname);
        println(" Var index: " + index);
        println(" Signature: " + lv.type().signature());
    }
    void printFrameVariables(StackFrame frame) throws Exception {
        int index = 0;
        List localVars = frame.visibleVariables();
        println("\n Visible variables at this point are: ");
        for (Iterator it = localVars.iterator(); it.hasNext();) {
            LocalVariable lv = (LocalVariable) it.next();
            printOneVariable(lv, index++);
            println(" Var value: " + frame.getValue(lv));
        }
    }
    BooleanValue incrValue(BooleanValue val) {
        boolean value = val.value();
        return vm().mirrorOf(!value);
    }
    ByteValue incrValue(ByteValue val) {
        byte value = val.value();
        return vm().mirrorOf(++value);
    }
    CharValue incrValue(CharValue val) {
        char value = val.value();
        return vm().mirrorOf(++value);
    }
    ShortValue incrValue(ShortValue val) {
        short value = val.value();
        return vm().mirrorOf(++value);
    }
    IntegerValue incrValue(IntegerValue val) {
        int value = val.value();
        return vm().mirrorOf(++value);
    }
    LongValue incrValue(LongValue val) {
        long value = val.value();
        return vm().mirrorOf(++value);
    }
    FloatValue incrValue(FloatValue val) {
        float value = val.value();
        return  vm().mirrorOf(++value);
    }
    DoubleValue incrValue(DoubleValue val) {
        double value = val.value();
        return vm().mirrorOf(++value);
    }
    StringReference incrValue(StringReference val) {
        String newstr = new String("Set String ").concat(val.value());
        return vm().mirrorOf(newstr);
    }
    void checkSetBooleanTypes(StackFrame frame, LocalVariable lv) throws Exception {
        BooleanValue get = (BooleanValue) frame.getValue(lv);
        BooleanValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (BooleanValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        boolean v1 = get.value();
        boolean v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetByteTypes(StackFrame frame, LocalVariable lv) throws Exception {
        ByteValue get = (ByteValue) frame.getValue(lv);
        ByteValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (ByteValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        byte v1 = get.value();
        byte v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetCharTypes(StackFrame frame, LocalVariable lv) throws Exception {
        CharValue get = (CharValue) frame.getValue(lv);
        CharValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (CharValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        char v1 = get.value();
        char v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetShortTypes(StackFrame frame, LocalVariable lv) throws Exception {
        ShortValue get = (ShortValue) frame.getValue(lv);
        ShortValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (ShortValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        short v1 = get.value();
        short v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetIntegerTypes(StackFrame frame, LocalVariable lv) throws Exception {
        IntegerValue get = (IntegerValue) frame.getValue(lv);
        IntegerValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (IntegerValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        int v1 = get.value();
        int v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetLongTypes(StackFrame frame, LocalVariable lv) throws Exception {
        LongValue get = (LongValue) frame.getValue(lv);
        LongValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (LongValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        long v1 = get.value();
        long v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetFloatTypes(StackFrame frame, LocalVariable lv) throws Exception {
        FloatValue get = (FloatValue) frame.getValue(lv);
        FloatValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (FloatValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        float v1 = get.value();
        float v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetDoubleTypes(StackFrame frame, LocalVariable lv) throws Exception {
        DoubleValue get = (DoubleValue) frame.getValue(lv);
        DoubleValue set = incrValue(get);
        frame.setValue(lv, set);
        get = (DoubleValue) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        double v1 = get.value();
        double v2 = set.value();
        if (v1 == v2) {
            println(" Success: Value was set correctly!");
        } else {
            failure(" Failure: Value was NOT set correctly!");
        }
        println("");
    }
    void checkSetStringTypes(StackFrame frame, LocalVariable lv) throws Exception {
        StringReference get = (StringReference) frame.getValue(lv);
        StringReference set = incrValue((StringReference) frame.getValue(lv));
        frame.setValue(lv, set);
        get = (StringReference) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        String str1 = get.value();
        String str2 = set.value();
        if (str1.compareTo(str2) == 0) {
            println(" Success: String was set correctly!");
        } else {
            failure(" Failure: String was NOT set correctly!");
        }
        println("");
    }
    void checkSetObjectTypes(StackFrame frame, LocalVariable lv) throws Exception {
        ObjectReference get = (ObjectReference) frame.getValue(lv);
        ObjectReference set = get; 
        frame.setValue(lv, set);
        get = (ObjectReference) frame.getValue(lv);
        println(" Var  Set: " + set);
        println(" Var  Get: " + get);
        println("");
        if (set.uniqueID() == get.uniqueID()) {
            println(" Success: Object was set correctly!");
        } else {
            failure(" Failure: Object was NOT set correctly!");
        }
        println("");
    }
    void negativeIntegerCheck(StackFrame frame, LocalVariable lv) throws Exception {
        try {
            IntegerValue get = (IntegerValue) frame.getValue(lv);
            println(" Get: No ClassCastException error!");
        } catch(java.lang.ClassCastException ex) {
            println(" Success: Get: ClassCastException error has cought as expected!");
        }
        try {
            IntegerValue set = vm().mirrorOf((int) 0x3F);
            frame.setValue(lv, set);
            println(" Set: No InvalidTypeException with Integer error!");
        } catch(com.sun.jdi.InvalidTypeException ex) {
            println(" Success: Set: InvalidTypeException with Integer error has cought as expected!");
        }
    }
    void negativeFloatCheck(StackFrame frame, LocalVariable lv) throws Exception {
        try {
            FloatValue get = (FloatValue) frame.getValue(lv);
            println(" Get: No ClassCastException error!");
        } catch(java.lang.ClassCastException ex) {
            println(" Success: Get: ClassCastException with Float error has cought as expected!");
        }
        try {
            FloatValue set = vm().mirrorOf(1.2345f);
            frame.setValue(lv, set);
            println(" Set: No InvalidTypeException with Float error!");
        } catch(com.sun.jdi.InvalidTypeException ex) {
            println(" Success: Set: InvalidTypeException error has cought as expected!");
        }
    }
    void negativeDoubleCheck(StackFrame frame, LocalVariable lv) throws Exception {
        try {
            DoubleValue get = (DoubleValue) frame.getValue(lv);
            println(" Get: No ClassCastException error!");
        } catch(java.lang.ClassCastException ex) {
            println(" Success: Get: ClassCastException  with Double error has cought as expected!");
        }
        try {
            DoubleValue set = vm().mirrorOf(1.2345E02);
            frame.setValue(lv, set);
            println(" Set: No InvalidTypeException with Double error!");
        } catch(com.sun.jdi.InvalidTypeException ex) {
            println(" Success: Set: InvalidTypeException error has cought as expected!");
        }
    }
    void checkSetFrameVariables(StackFrame frame) throws Exception {
        List localVars = frame.visibleVariables();
        int index = 0;
        println("\n Set variable values:");
        for (Iterator it = localVars.iterator(); it.hasNext();index++) {
            LocalVariable lv = (LocalVariable) it.next();
            String signature = lv.type().signature();
            switch (signature.charAt(0)) {
            case 'Z': 
                checkSetBooleanTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'B': 
                checkSetByteTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'C': 
                checkSetCharTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'S': 
                checkSetShortTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'I': 
                checkSetIntegerTypes(frame, lv);
                if (index > 0) { 
                    negativeFloatCheck(frame, lv);
                    negativeDoubleCheck(frame, lv);
                }
                break;
            case 'J': 
                checkSetLongTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'F': 
                checkSetFloatTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeDoubleCheck(frame, lv);
                break;
            case 'D': 
                checkSetDoubleTypes(frame, lv);
                negativeIntegerCheck(frame, lv);
                negativeFloatCheck(frame, lv);
                break;
            case 'L':
                if (signature.compareTo("Ljava/lang/String;") == 0) {
                    checkSetStringTypes(frame, lv);
                    negativeIntegerCheck(frame, lv);
                    negativeFloatCheck(frame, lv);
                }
                if (signature.compareTo("Ljava/lang/Object;") == 0) {
                    checkSetObjectTypes(frame, lv);
                    negativeIntegerCheck(frame, lv);
                    negativeFloatCheck(frame, lv);
                }
                break;
            default:
                printOneVariable(lv, index);
                failure(" Failure: List of local variables has a wrong entry!");
            };
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("GetSetLocalTarg");
        println("startToMain(GetSetLocalTarg)");
        List localVars = printAllVariables("GetSetLocalTarg", "staticMeth");
        targetClass = bpe.location().declaringType();
        println("targetClass");
        mainThread = bpe.thread();
        println("mainThread");
        EventRequestManager erm = vm().eventRequestManager();
        println("EventRequestManager");
        StackFrame frame = null;
        for (int line = 38; line < 118; line += 4) {
            println("\n resumeTo(GetSetLocalTarg, " + line + ")");
            bpe = resumeTo("GetSetLocalTarg", line);
            frame = bpe.thread().frame(0);
            printFrameVariables(frame);
            checkSetFrameVariables(frame);
        }
        checkGetSetAllVariables(localVars, frame);
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("GetSetLocalTest: passed");
        } else {
            throw new Exception("GetSetLocalTest: failed");
        }
    }
}

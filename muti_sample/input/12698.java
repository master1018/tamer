class Testy {
    int field1;
    int field2;
    int field3;
    int field4;
    int field5;
    int field6;
    final static int field7 = 7;  
    Testy() {
    }
}
class BacktraceFieldTarg {
    public static void gus() {
    }
    public static void main(String[] args) {
        Testy myTesty = new Testy();
        try {
            throw new RuntimeException("jjException");
        } catch (Exception ee) {
            gus();
            System.out.println("debuggee: Exception: " + ee);
        }
    }
}
public class BacktraceFieldTest extends TestScaffold {
    ThreadReference mainThread;
    BacktraceFieldTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new BacktraceFieldTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startTo("BacktraceFieldTarg", "gus", "()V");
        mainThread = bpe.thread();
        StackFrame myFrame = mainThread.frame(1);
        LocalVariable lv = myFrame.visibleVariableByName("ee");
        println("BT: lv = " + lv);
        println("BT: lvType = " + lv.typeName());
        List allFields = ((ReferenceType)(lv.type())).allFields();
        println("BT: allFields = " + allFields);
        Iterator iter = allFields.iterator();
        while(iter.hasNext()) {
            Field ff = (Field)iter.next();
            if (ff.toString().equals("java.lang.Throwable.backtrace")) {
                failure("ERROR: java.lang.Throwable.backtrace field not filtered out.");
                if (1 == 0) {
                    ObjectReference myVal = (ObjectReference)myFrame.getValue(lv);
                    println("BT: myVal = " + myVal);
                    ArrayReference backTraceVal = null;
                    backTraceVal = (ArrayReference)myVal.getValue(ff);
                    println("BT: backTraceVal = " + backTraceVal);
                    ArrayReference secondVal = (ArrayReference)backTraceVal.getValue(1);
                    println("BT: secondVal = " + secondVal);
                    Object x2Val = (Object)secondVal.getValue(0);
                    println("BT: x2Val = " + x2Val);
                    ArrayReference firstVal = (ArrayReference)backTraceVal.getValue(0);
                    println("BT: firstVal = " + firstVal);
                    Object xVal = (Object)firstVal.getValue(0);
                    println("BT: xVal = " + xVal);
                }
                break;
            }
        }
        if (!testFailed) {
            lv = myFrame.visibleVariableByName("myTesty");
            allFields = ((ReferenceType)(lv.type())).allFields();
            println("BT: allFields = " + allFields);
            if (allFields.size() != Testy.field7) {
                failure("ERROR: wrong number of fields; expected " + Testy.field7 + ", Got " + allFields.size());
            } else {
                iter = allFields.iterator();
                while(iter.hasNext()) {
                    String fieldName = ((Field)iter.next()).toString();
                    if (!fieldName.startsWith("Testy.field", 0)) {
                        failure("ERROR: Found bogus field: " + fieldName.toString());
                    }
                }
            }
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("BacktraceFieldTest: passed");
        } else {
            throw new Exception("BacktraceFieldTest: failed");
        }
    }
}

class GenericsTarg {
    static Gen1<String> genField = new Gen1<String>();;
    static Sub1 sub1Field = new Sub1();
    String[] strArray = null;
    int intField = 0;
    Object objField;
    public static void main(String[] args){
        System.out.println("Goodbye from GenericsTarg!");
    }
}
class Gen1<tt> {
    tt field1;
    Gen1() {
        System.out.println("Gen1<tt> ctor called");
    }
    tt method1(tt p1) {
        Gen1<String> xxx = null;
        System.out.println("method1: param is " + p1);
        return p1;
    }
    String method2() {
        String str = "This local variable is not generic";
        return str;
    }
}
class Sub1 extends Gen1<String> {
    String method1(String p1) {
        System.out.println("method1 has been overridden: param is " + p1);
        return "hi";
    }
}
public class GenericsTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    static boolean useOld;
    GenericsTest (String args[]) {
        super(args);
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 1 && args[0].equals("-xjdk")) {
            System.setProperty("java.home", args[1]);
            useOld = true;
            String[] args1 = new String[args.length - 2];
            for (int ii = 0; ii < args.length -2; ii++) {
                args1[ii] = args[ii + 2];
            }
            args = args1;
        }
        new GenericsTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("GenericsTarg");
        targetClass = bpe.location().declaringType();
        {
            Field strArray = targetClass.fieldByName("strArray");
            ReferenceType fieldType = (ReferenceType)(strArray.type());
            String genSig = fieldType.genericSignature();
            System.out.println("strArray name = " + strArray);
            System.out.println("         type = " + fieldType);
            System.out.println("          sig = " + fieldType.signature());
            System.out.println("       genSig = " + genSig);
            if (!useOld && genSig != null) {
                failure("FAILED: Expected generic signature = null for "
                        + fieldType.name() + ", received: " + genSig);
            }
        }
        {
            Field intField = targetClass.fieldByName("intField");
            Type fieldType = (Type)(intField.type());
            System.out.println("intField name = " + intField);
            System.out.println("         type = " + fieldType);
            System.out.println("          sig = " + fieldType.signature());
        }
        Field genField = targetClass.fieldByName("genField");
        ReferenceType gen1Class = (ReferenceType)(genField.type());
        String genSig;
        String expected;
        {
            expected = "<tt:Ljava/lang/Object;>Ljava/lang/Object;";
            genSig = gen1Class.genericSignature();
            System.out.println("genField name = " + genField);
            System.out.println("         type = " + gen1Class);
            System.out.println("          sig = " + gen1Class.signature());
            System.out.println("       genSig = " + genSig);
            if (!useOld && !expected.equals(genSig)) {
                failure("FAILED: Expected generic signature for gen1: " +
                        expected + ", received: " + genSig);
            }
        }
        {
            List genFields = gen1Class.fields();
            Field field1 = (Field)genFields.get(0);
            expected = "Ttt;";
            genSig = field1.genericSignature();
            System.out.println("field1 name = " + field1);
            System.out.println("       type = " + gen1Class.signature());
            System.out.println("        sig = " + field1.signature());
            System.out.println("    gen sig = " + genSig);
            if (!useOld && !expected.equals(genSig)) {
                failure("FAILED: Expected generic signature for field1: " +
                        expected + ", received: " + genSig);
            }
        }
        {
            List genMethods = gen1Class.methodsByName("method1");
            Method method1 = (Method)genMethods.get(0);
            expected = "(Ttt;)Ttt;";
            genSig = method1.genericSignature();
            System.out.println("method1 name = " + method1);
            System.out.println("        type = " + gen1Class.signature());
            System.out.println("         sig = " + method1.signature());
            System.out.println("     gen sig = " + genSig);
            System.out.println("     bridge  = " + method1.isBridge());
            if (!useOld && !expected.equals(genSig)) {
                failure("FAILED: Expected generic signature for method1: " +
                        expected + ", received: " + genSig);
            }
            if (method1.isBridge()) {
                failure("FAILED: Expected gen1.method1 to not be a bridge"
                         + " method but it is");
            }
            List localVars = method1.variables();
            String[] expectedGenSigs = { "Ttt", "Gen1<String>" };
            for ( int ii = 0 ; ii < localVars.size(); ii++) {
                expected = expectedGenSigs[ii];
                LocalVariable pp = (LocalVariable)localVars.get(ii);
                genSig = pp.genericSignature();
                System.out.println("   local var " + ii + " = " + pp.name());
                System.out.println("      sig      = " + pp.signature());
                System.out.println("      gen sig  = " + genSig);
            }
        }
        {
            List genMethods = gen1Class.methodsByName("method2");
            Method method2 = (Method)genMethods.get(0);
            expected = "null";
            genSig = method2.genericSignature();
            genSig = (genSig == null) ? "null" : genSig;
            System.out.println("method2 name = " + method2);
            System.out.println("        type = " + gen1Class.signature());
            System.out.println("         sig = " + method2.signature());
            System.out.println("     gen sig = " + genSig);
            System.out.println("     bridge  = " + method2.isBridge());
            if (!useOld && !expected.equals(genSig)) {
                failure("FAILED: Expected generic signature for method2: " +
                        expected + ", received: " + genSig);
            }
            if (method2.isBridge()) {
                failure("FAILED: Expected gen1.method2 to not be a bridge"
                         + " method but it is");
            }
            List localVars = method2.variables();
            expected = "null";
            for ( int ii = 0 ; ii < localVars.size(); ii++) {
                LocalVariable pp = (LocalVariable)localVars.get(ii);
                genSig = pp.genericSignature();
                genSig = (genSig == null) ? "null" : genSig;
                System.out.println("   local var " + ii + " = " + pp.name());
                System.out.println("      sig      = " + pp.signature());
                System.out.println("      gen sig  = " + genSig);
                if (!useOld && !expected.equals(genSig)) {
                   failure("FAILED: Expected generic signature for local var: " +
                           expected + ", received: " + genSig);
                }
            }
        }
        {
            Field sub1Field = targetClass.fieldByName("sub1Field");
            ReferenceType sub1Class = (ReferenceType)(sub1Field.type());
            List<Method> sub1Methods = sub1Class.methodsByName("method1");
            for (Method mm: sub1Methods) {
                System.out.println("method is: " + mm);
            }
            Method method1 = (Method)sub1Methods.get(1);
            System.out.println("\nmethod1 name = " + method1);
            System.out.println("         sig = " + method1.signature());
            System.out.println("      bridge = " + method1.isBridge());
            if (!useOld && !method1.isBridge()) {
                failure("FAILED: Expected Sub1.method1 to be a bridge method"
                         + " but it isn't");
            }
        }
        {
            genSig = targetClass.genericSignature();
            if (genSig != null) {
                failure("FAILED: Expected generic signature = null for "
                        + targetClass.name() + ", received: " + genSig);
            }
        }
        {
            Field objField = targetClass.fieldByName("objField");
            genSig = objField.genericSignature();
            if (genSig != null) {
                failure("FAILED: Expected generic signature = null for "
                        + objField.name() + ", received: " + genSig);
            }
        }
        {
            List methods = targetClass.methodsByName("main");
            Method main = (Method)methods.get(0);
            genSig = main.genericSignature();
            if (genSig != null) {
                failure("FAILED: Expected generic signature = null for "
                        + main.name() + ", received: " + genSig);
            }
        }
        if (0 == 1) {
            mainThread = bpe.thread();
            EventRequestManager erm = vm().eventRequestManager();
            StepRequest request = erm.createStepRequest(mainThread,
                                                    StepRequest.STEP_LINE,
                                                    StepRequest.STEP_INTO);
            request.enable();
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("GenericsTest: passed");
        } else {
            throw new Exception("GenericsTest: failed");
        }
    }
}

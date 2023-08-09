class VarargsTarg {
    static String[] strArray = new String[] {"a", "b"};
    static int[] intArray = new int[] {1, 2};
    static VarargsTarg vt1 = new VarargsTarg("vt1", "");
    static VarargsTarg vt2 = new VarargsTarg("vt2", "");
    String iname;
    VarargsTarg(String ... name) {
        iname = "";
        for (int ii = 0; ii < name.length; ii++) {
            iname += name[ii];
        }
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("debuggee: " + varString());
        System.out.println("debuggee: " + varString(null));
        System.out.println("debuggee: " + varString("a"));
        System.out.println("debuggee: " + varString("b", "c"));
        System.out.println("debuggee: " + fixedString(null));
        System.out.println("debuggee: " + vt1.varStringInstance(vt1, vt2));
        System.out.println("debuggge: " + varInt(1, 2, 3));
        System.out.println("debuggee: " + varInteger( new Integer(89)));
        System.out.println("debugggee: " + varInteger(3, 5, 6));
        System.out.println("Goodbye from VarargsTarg!");
        bkpt();
    }
    static void bkpt() {
    }
    static String fixedInt(int p1) {
        return "" + p1;
    }
    static String fixedInteger(Integer p1) {
        return "" + p1;
    }
     static String varInt(int... ss) {
         String retVal = "";
         for (int ii = 0; ii < ss.length; ii++) {
             retVal += ss[ii];
         }
         return retVal;
     }
    static String varInteger(Integer... ss) {
        String retVal = "";
        for (int ii = 0; ii < ss.length; ii++) {
            retVal += ss[ii];
        }
        return retVal;
    }
    static String varString(String... ss) {
        if (ss == null) {
            return "-null-";
        }
        String retVal = "";
        for (int ii = 0; ii < ss.length; ii++) {
            retVal += ss[ii];
        }
        return retVal;
    }
    static String varString2(int p1, String... ss) {
        return p1 + varString(ss);
    }
    static String fixedString(String ss) {
        return "-fixed-";
    }
    String varStringInstance(VarargsTarg... args) {
        if (args == null) {
            return "-null-";
        }
        String retVal = iname + ": ";
        for (int ii = 0; ii < args.length; ii++) {
            retVal += args[ii].iname;
        }
        return retVal;
    }
}
public class VarargsTest extends TestScaffold {
    ClassType targetClass;
    ThreadReference mainThread;
    VarargsTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new VarargsTest(args).startTests();
    }
    void fail(String reason) {
        failure(reason);
    }
    void doInvoke(Object ct, Method mm, List args, Object expected) {
        StringReference returnValue = null;
        try {
            returnValue = doInvokeNoVerify(ct, mm, args);
        } catch (Exception ee) {
            fail("failure: invokeMethod got exception : " + ee);
            ee.printStackTrace();
            return;
        }
        if (!returnValue.value().equals(expected)) {
            fail("failure: expected \"" + expected + "\", got \"" +
                 returnValue.value() + "\"");
        }
    }
    StringReference doInvokeNoVerify(Object ct, Method mm, List args)
        throws Exception {
        StringReference returnValue = null;
        if (ct instanceof ClassType) {
            returnValue = (StringReference)((ClassType)ct).
                invokeMethod(mainThread, mm, args, 0);
        } else {
            returnValue = (StringReference)((ObjectReference)ct).
                invokeMethod(mainThread, mm, args, 0);
        }
        return returnValue;
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("VarargsTarg");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        bpe = resumeTo("VarargsTarg", "bkpt", "()V");
        ReferenceType rt = findReferenceType("VarargsTarg");
        List mList;
        mList = rt.methodsByName("varString");
        Method varString = (Method)mList.get(0);
        mList = rt.methodsByName("varString2");
        Method varString2 = (Method)mList.get(0);
        if (!varString.isVarArgs()) {
            fail("failure: varString is not flagged as being var args");
        }
        if (!varString2.isVarArgs()) {
            fail("failure: varString2 is not flagged as being var args");
        }
        {
            ArrayList nullArg1 = new ArrayList(0);
            doInvoke(targetClass, varString, nullArg1,  "");
        }
        {
            ArrayList nullArg1 = new ArrayList(1);
            nullArg1.add(null);
            doInvoke(targetClass, varString, nullArg1,  "-null-");
        }
        {
            ArrayList nullArg2 = new ArrayList(1);
            nullArg2.add(vm().mirrorOf(9));
            doInvoke(targetClass, varString2, nullArg2,  "9");
        }
        {
            ArrayList nullArg2 = new ArrayList(2);
            nullArg2.add(vm().mirrorOf(9));
            nullArg2.add(null);
            doInvoke(targetClass, varString2, nullArg2,  "9-null-");
        }
        {
            ArrayList args1 = new ArrayList(4);
            args1.add(vm().mirrorOf("1"));
            doInvoke(targetClass, varString, args1, "1");
            args1.add(vm().mirrorOf("2"));
            args1.add(vm().mirrorOf("3"));
            args1.add(vm().mirrorOf("4"));
            doInvoke(targetClass, varString, args1, "1234");
        }
        {
            ArrayList args2 = new ArrayList(2);
            args2.add(vm().mirrorOf(9));
            args2.add(vm().mirrorOf("1"));
            doInvoke(targetClass, varString2, args2, "91");
            args2.add(vm().mirrorOf("2"));
            doInvoke(targetClass, varString2, args2, "912");
        }
        {
            Field ff = targetClass.fieldByName("strArray");
            Value vv1 = targetClass.getValue(ff);
            ArrayList argsArray = new ArrayList(1);
            argsArray.add(vv1);
            doInvoke(targetClass, varString, argsArray, "ab");
            argsArray.add(vm().mirrorOf("x"));
            boolean isOk = false;
            try {
                doInvokeNoVerify(targetClass, varString, argsArray);
            } catch (Exception ee) {
                isOk = true;
            }
            if (!isOk) {
                fail("failure: an array and a String didn't cause an exception");
            }
        }
        {
            Field vtField = targetClass.fieldByName("vt1");
            Value vv1 = targetClass.getValue(vtField);
            vtField = targetClass.fieldByName("vt2");
            Value vv2 = targetClass.getValue(vtField);
            Value vv3;
            {
                mList = rt.methodsByName("<init>");
                Method ctor = (Method)mList.get(0);
                if (!ctor.isVarArgs()) {
                    fail("failure: Constructor is not varargs");
                }
                ArrayList argsArray = new ArrayList(2);
                argsArray.add(vm().mirrorOf("vt3"));
                argsArray.add(vm().mirrorOf("xx"));
                vv3 = targetClass.newInstance(mainThread, ctor, argsArray, 0);
            }
            mList = rt.methodsByName("varStringInstance");
            Method varStringInstance = (Method)mList.get(0);
            ArrayList argsArray = new ArrayList(3);
            argsArray.add(vv1);
            argsArray.add(vv2);
            argsArray.add(vv3);
            doInvoke(vv1, varStringInstance, argsArray, "vt1: vt1vt2vt3xx");
        }
        {
            List mlist;
            Method mm;
            ArrayList ll = new ArrayList(2);
            mlist = rt.methodsByName("fixedInt");
            mm = (Method)mlist.get(0);
            ll.add(vm().mirrorOf(21));
            doInvoke(targetClass, mm, ll, "21");
            mlist = rt.methodsByName("varInt");
            mm = (Method)mlist.get(0);
            Field ff = targetClass.fieldByName("intArray");
            Value vv1 = targetClass.getValue(ff);
            ll.set(0, vv1);
            doInvoke(targetClass, mm, ll, "12");
            ll.set(0, vm().mirrorOf(21));
            ll.add(vm().mirrorOf(22));
            doInvoke(targetClass, mm, ll, "2122");
            mlist = rt.methodsByName("varInteger");
            mm = (Method)mlist.get(0);
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("VarargsTest: passed");
        } else {
            throw new Exception("VarargsTest: failed");
        }
    }
}

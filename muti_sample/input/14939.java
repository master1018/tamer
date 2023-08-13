class UTF8Targ {
    static String[] vals = new String[] {"xx\u0000yy",           
                                         "xx\ud800\udc00yy",     
                                         "xx\udbff\udfffyy"      
    };
    static String aField;
    public static void main(String[] args){
        System.out.println("Howdy!");
        gus();
        System.out.println("Goodbye from UTF8Targ!");
    }
    static void gus() {
    }
}
public class UTF8Test extends TestScaffold {
    ClassType targetClass;
    ThreadReference mainThread;
    Field targetField;
    UTF8Test (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new UTF8Test(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("UTF8Targ");
        targetClass = (ClassType)bpe.location().declaringType();
        targetField = targetClass.fieldByName("aField");
        ArrayReference targetVals = (ArrayReference)targetClass.getValue(targetClass.fieldByName("vals"));
        for (int ii = 0; ii < UTF8Targ.vals.length; ii++) {
            StringReference val = (StringReference)targetVals.getValue(ii);
            String valStr = val.value();
            if (!valStr.equals(UTF8Targ.vals[ii]) ||
                valStr.length() != UTF8Targ.vals[ii].length()) {
                failure("     FAILED: Expected /" + printIt(UTF8Targ.vals[ii]) +
                        "/, but got /" + printIt(valStr) + "/, length = " + valStr.length());
            }
        }
        doFancyVersion();
        resumeTo("UTF8Targ", "gus", "()V");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ee) {
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("UTF8Test: passed");
        } else {
            throw new Exception("UTF8Test: failed");
        }
    }
    void doFancyVersion() throws Exception {
        for (int ii = Character.MIN_CODE_POINT;
             ii < Character.MIN_SUPPLEMENTARY_CODE_POINT;
             ii += 4) {
            if (ii == Character.MIN_SURROGATE) {
                ii = Character.MAX_SURROGATE - 3;
                break;
            }
            doFancyTest(ii, ii + 1, ii + 2, ii + 3);
        }
        for (int ii = Character.MIN_SUPPLEMENTARY_CODE_POINT;
             ii <= Character.MAX_CODE_POINT;
             ii += 2000) {
            doFancyTest(ii, ii + 1, ii + 2, ii + 3);
        }
    }
    void doFancyTest(int ... args) throws Exception {
        String ss = new String(args, 0, 4);
        targetClass.setValue(targetField, vm().mirrorOf(ss));
        StringReference returnedVal = (StringReference)targetClass.getValue(targetField);
        String returnedStr = returnedVal.value();
        if (!ss.equals(returnedStr)) {
            failure("Set: FAILED: Expected /" + printIt(ss) +
                    "/, but got /" + printIt(returnedStr) + "/, length = " + returnedStr.length());
        }
    }
     String printIt(String arg) {
        char[] carray = arg.toCharArray();
        StringBuffer bb = new StringBuffer(arg.length() * 5);
        for (int ii = 0; ii < arg.length(); ii++) {
            int ccc = arg.charAt(ii);
            bb.append(String.format("%1$04x ", ccc));
        }
        return bb.toString();
    }
    String printIt1(String arg) {
        byte[] barray = null;
        try {
             barray = arg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ee) {
        }
        StringBuffer bb = new StringBuffer(barray.length * 3);
        for (int ii = 0; ii < barray.length; ii++) {
            bb.append(String.format("%1$02x ", barray[ii]));
        }
        return bb.toString();
    }
}

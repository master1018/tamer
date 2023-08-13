class EarlyReturnNegativeTarg {
    static URL[] urls = new URL[1];
    public static byte      byteValue = 89;
    public static char      charValue = 'x';
    public static double    doubleValue = 2.2;
    public static float     floatValue = 3.3f;
    public static int       intValue = 1;
    public static long      longValue = Long.MAX_VALUE;
    public static short     shortValue = 8;
    public static boolean   booleanValue = false;
    public static Class       classValue = Object.class;
    public static ClassLoader classLoaderValue;
    {
        try {
            urls[0] = new URL("hi there");
        } catch (java.net.MalformedURLException ee) {
        }
        classLoaderValue = new URLClassLoader(urls);
    }
    public static Thread      threadValue = Thread.currentThread();
    public static ThreadGroup threadGroupValue = threadValue.getThreadGroup();
    public static String      stringValue = "abc";
    public static int[]       intArrayValue = new int[] {1, 2, 3};
    public static Object[]    objectArrayValue = new Object[] {"a", "b", "c"};
    public static EarlyReturnNegativeTarg  objectValue =
        new EarlyReturnNegativeTarg();
    public String ivar = stringValue;
    public static String s_show(String p1) { return p1;}
    public byte i_bytef()            { return byteValue; }
    public char i_charf()            { return charValue; }
    public double i_doublef()        { return doubleValue; }
    public float i_floatf()          { return floatValue; }
    public int i_intf()              { return intValue; }
    public long i_longf()            { return longValue; }
    public short i_shortf()          { return shortValue; }
    public boolean i_booleanf()      { return booleanValue; }
    public String i_stringf()        { return stringValue; }
    public Class i_classf()          { return classValue; }
    public ClassLoader i_classLoaderf()
                                     { return classLoaderValue; }
    public Thread i_threadf()        { return threadValue; }
    public ThreadGroup i_threadGroupf()
                                     { return threadGroupValue; }
    public int[] i_intArrayf()       { return intArrayValue; }
    public Object[] i_objectArrayf() { return objectArrayValue; }
    public Object i_nullObjectf()    { return null; }
    public Object i_objectf()        { return objectValue; }
    public void i_voidf()            {}
    static void doit(EarlyReturnNegativeTarg xx) throws Exception {
        System.err.print("debugee in doit ");
        s_show("==========  Testing instance methods ================");
        xx.i_bytef();
        xx.i_charf();
        xx.i_doublef();
        xx.i_floatf();
        xx.i_intf();
        xx.i_longf();
        xx.i_shortf();
        xx.i_booleanf();
        xx.i_stringf();
        xx.i_intArrayf();
        xx.i_objectArrayf();
        xx.i_classf();
        xx.i_classLoaderf();
        xx.i_threadf();
        xx.i_threadGroupf();
        xx.i_nullObjectf();
        xx.i_objectf();
        xx.i_voidf();
    }
    public static void main(String[] args) throws Exception {
        System.err.println("debugee in main");
        EarlyReturnNegativeTarg xx =
            new EarlyReturnNegativeTarg();
        doit(xx);
    }
}
public class EarlyReturnNegativeTest extends TestScaffold {
    static VirtualMachineManager vmm ;
    ClassType targetClass;
    Field theValueField;
    ByteValue byteVV;
    CharValue charVV;
    DoubleValue doubleVV;
    FloatValue floatVV;
    IntegerValue integerVV;
    LongValue longVV;
    ShortValue shortVV;
    BooleanValue booleanVV;
    ObjectReference objectVV;
    ArrayReference intArrayVV;
    ArrayReference objectArrayVV;
    VoidValue voidVV;
    EarlyReturnNegativeTest(String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        EarlyReturnNegativeTest meee = new EarlyReturnNegativeTest(args);
        vmm = Bootstrap.virtualMachineManager();
        meee.startTests();
    }
    public BreakpointRequest setBreakpoint(String clsName,
                                           String methodName,
                                           String methodSignature) {
        ReferenceType rt = findReferenceType(clsName);
        if (rt == null) {
            rt = resumeToPrepareOf(clsName).referenceType();
        }
        Method method = findMethod(rt, methodName, methodSignature);
        if (method == null) {
            throw new IllegalArgumentException("Bad method name/signature");
        }
        BreakpointRequest bpr = eventRequestManager().createBreakpointRequest(method.location());
        bpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        bpr.enable();
        return bpr;
    }
    void doEarly(ThreadReference tr, String methodName, Value val) {
        try {
            tr.forceEarlyReturn(val);
        } catch (InvalidTypeException ex) {
            System.out.println("Ok: " + methodName);
            return;
        } catch (Exception ex) {
            failure("failure: " + ex.toString());
            ex.printStackTrace();
            return;
        }
        failure("Expected InvalidTypeException for " + methodName + ", " + val + " but didn't get it.");
    }
    public void breakpointReached(BreakpointEvent event) {
        String origMethodName = event.location().method().name();
        String methodName = origMethodName.substring(2);
        ThreadReference tr = event.thread();
        if (vmm.majorInterfaceVersion() >= 1 &&
            vmm.minorInterfaceVersion() >= 6 &&
            vm().canForceEarlyReturn()) {
            if ("shortf".equals(methodName)){
                doEarly(tr, origMethodName, booleanVV);
                doEarly(tr, origMethodName, objectVV);
                doEarly(tr, origMethodName, voidVV);
                doEarly(tr, origMethodName, intArrayVV);
                doEarly(tr, origMethodName, objectArrayVV);
            } else if ("booleanf".equals(methodName)) {
                doEarly(tr, origMethodName, shortVV);
                doEarly(tr, origMethodName, objectVV);
                doEarly(tr, origMethodName, voidVV);
                doEarly(tr, origMethodName, intArrayVV);
                doEarly(tr, origMethodName, objectArrayVV);
            } else if ("intArrayf".equals(methodName)) {
                doEarly(tr, origMethodName, booleanVV);
                doEarly(tr, origMethodName, shortVV);
                doEarly(tr, origMethodName, voidVV);
                doEarly(tr, origMethodName, objectVV);
                doEarly(tr, origMethodName, objectArrayVV);
            } else if ("objectArrayf".equals(methodName)) {
                doEarly(tr, origMethodName, booleanVV);
                doEarly(tr, origMethodName, shortVV);
                doEarly(tr, origMethodName, voidVV);
                doEarly(tr, origMethodName, objectVV);
                doEarly(tr, origMethodName, intArrayVV);
            } else if ("objectf".equals(methodName)) {
                doEarly(tr, origMethodName, booleanVV);
                doEarly(tr, origMethodName, shortVV);
                doEarly(tr, origMethodName, voidVV);
             } else if ("voidf".equals(methodName)) {
                doEarly(tr, origMethodName, booleanVV);
                doEarly(tr, origMethodName, shortVV);
                doEarly(tr, origMethodName, objectVV);
                doEarly(tr, origMethodName, intArrayVV);
                doEarly(tr, origMethodName, objectArrayVV);
            } else {
                System.out.println("Ignoring: " + methodName);
                return;
            }
        } else {
            System.out.println("Cannot force early return for method: " + origMethodName);
        }
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("EarlyReturnNegativeTarg");
        targetClass = (ClassType)bpe.location().declaringType();
        mainThread = bpe.thread();
        setBreakpoint("EarlyReturnNegativeTarg", "i_bytef", "()B");
        setBreakpoint("EarlyReturnNegativeTarg", "i_charf", "()C");
        setBreakpoint("EarlyReturnNegativeTarg", "i_doublef", "()D");
        setBreakpoint("EarlyReturnNegativeTarg", "i_floatf", "()F");
        setBreakpoint("EarlyReturnNegativeTarg", "i_intf", "()I");
        setBreakpoint("EarlyReturnNegativeTarg", "i_longf", "()J");
        setBreakpoint("EarlyReturnNegativeTarg", "i_shortf", "()S");
        setBreakpoint("EarlyReturnNegativeTarg", "i_booleanf", "()Z");
        setBreakpoint("EarlyReturnNegativeTarg", "i_stringf", "()Ljava/lang/String;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_intArrayf", "()[I");
        setBreakpoint("EarlyReturnNegativeTarg", "i_objectArrayf", "()[Ljava/lang/Object;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_classf", "()Ljava/lang/Class;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_classLoaderf", "()Ljava/lang/ClassLoader;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_threadf", "()Ljava/lang/Thread;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_threadGroupf", "()Ljava/lang/ThreadGroup;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_nullObjectf", "()Ljava/lang/Object;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_objectf", "()Ljava/lang/Object;");
        setBreakpoint("EarlyReturnNegativeTarg", "i_voidf", "()V");
        Field theValueField = targetClass.fieldByName("byteValue");
        byteVV = (ByteValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("charValue");
        charVV = (CharValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("doubleValue");
        doubleVV = (DoubleValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("floatValue");
        floatVV = (FloatValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("intValue");
        integerVV = (IntegerValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("longValue");
        longVV = (LongValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("shortValue");
        shortVV = (ShortValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("booleanValue");
        booleanVV = (BooleanValue)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("objectValue");
        objectVV = (ObjectReference)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("intArrayValue");
        intArrayVV = (ArrayReference)targetClass.getValue(theValueField);
        theValueField = targetClass.fieldByName("objectArrayValue");
        objectArrayVV = (ArrayReference)targetClass.getValue(theValueField);
        voidVV = vm().mirrorOfVoid();
        listenUntilVMDisconnect();
        if (!testFailed) {
            System.out.println();
            System.out.println("EarlyReturnNegativeTest: passed");
        } else {
            System.out.println();
            System.out.println("EarlyReturnNegativeTest: failed");
            throw new Exception("EarlyReturnNegativeTest: failed");
        }
    }
}

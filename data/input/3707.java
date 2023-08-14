class DataModelTarg {
    static String dataModel;
    public DataModelTarg () {
        dataModel = System.getProperty("sun.arch.data.model");
    }
    public void ready () {
        System.out.println("sun.arch.data.model is: " + dataModel);
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        DataModelTarg my = new DataModelTarg();
        my.ready();
        System.out.println("Goodbye from DataModelTarg!");
    }
}
public class DataModelTest extends TestScaffold {
    DataModelTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new DataModelTest(args).startTests();
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startTo("DataModelTarg", "ready", "()V");
        ObjectReference targetObject = bpe.thread().frame(0).thisObject();
        ReferenceType rt = targetObject.referenceType();
        Field field = rt.fieldByName("dataModel");
        Value v = targetObject.getValue(field);
        StringReference sv = (StringReference) v;
        String expectedValue = System.getProperty("EXPECTED", "32");
        if (!expectedValue.equals(sv.value())) {
            failure("Expecting sun.arch.data.model = " + expectedValue +
                    " but got " + sv.value() + " instead.");
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("DataModelTest: passed");
        } else {
            throw new Exception("DataModelTest: failed");
        }
    }
}

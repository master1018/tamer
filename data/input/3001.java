class A extends Object {
    public static int aField = 0;
}
class B extends A {
}
class FieldWatchpointsDebugee {
    public void update (){
        A.aField = 7;
        B.aField = 11;
    }
    public void access (){
        System.out.print("aField is: ");
        System.out.println(A.aField);
    }
    public static void main(String[] args){
        A testA = new A();
        B testB = new B();
        FieldWatchpointsDebugee my =
            new FieldWatchpointsDebugee();
        my.update();
        my.access();
    }
}
public class FieldWatchpoints extends TestScaffold {
    boolean fieldModifyReported = false;
    boolean fieldAccessReported = false;
    FieldWatchpoints (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new FieldWatchpoints (args).startTests();
    }
    protected void runTests() throws Exception {
        startTo("FieldWatchpointsDebugee", "update", "()V");
        try {
            ReferenceType rt = findReferenceType("A");
            String fieldName = "aField";
            Field field = rt.fieldByName(fieldName);
            if (field == null) {
                throw new Exception ("Field name not found: " + fieldName);
            }
            com.sun.jdi.request.EventRequest req =
               eventRequestManager().createModificationWatchpointRequest(field);
            req.setSuspendPolicy(com.sun.jdi.request.EventRequest.SUSPEND_ALL);
            req.enable();
            req =
               eventRequestManager().createAccessWatchpointRequest(field);
            req.setSuspendPolicy(com.sun.jdi.request.EventRequest.SUSPEND_ALL);
            req.enable();
            addListener (new TargetAdapter() {
                    EventSet lastSet = null;
                    public void eventSetReceived(EventSet set) {
                        lastSet = set;
                    }
                    public void fieldModified(ModificationWatchpointEvent event) {
                        System.out.println("Field modified: " + event);
                        fieldModifyReported = true;
                        lastSet.resume();
                    }
                    public void fieldAccessed(AccessWatchpointEvent event) {
                        System.out.println("Field accessed: " + event);
                        fieldAccessReported = true;
                        lastSet.resume();
                    }
                });
            vm().resume();
        } catch (Exception ex){
            ex.printStackTrace();
            testFailed = true;
        } finally {
            resumeToVMDisconnect();
        }
        if (!testFailed && fieldModifyReported && fieldAccessReported) {
            System.out.println("FieldWatchpoints: passed");
        } else {
            throw new Exception("FieldWatchpoints: failed");
        }
    }
}

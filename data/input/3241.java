public class NativeInstanceFilter extends JDIScaffold {
    static int unfilteredEvents = 0;
    public static void main(String args[]) throws Exception {
        new NativeInstanceFilter().startTests();
    }
    public NativeInstanceFilter() {
        super();
    }
    static EventRequestManager requestManager = null;
    static MethodExitRequest request = null;
    private void listen() {
        TargetAdapter adapter = new TargetAdapter() {
            EventSet set = null;
            ObjectReference instance = null;
            public boolean eventSetReceived(EventSet set) {
                this.set = set;
                return false;
            }
            public boolean methodExited(MethodExitEvent event) {
                String name = event.method().name();
                if (instance == null && name.equals("latch")) {
                    System.out.println("Setting up instance filter");
                    instance = (ObjectReference)event.returnValue();
                    requestManager.deleteEventRequest(request);
                    request = requestManager.createMethodExitRequest();
                    request.addInstanceFilter(instance);
                    request.enable();
                } else if (instance != null && name.equals("intern")) {
                    System.out.println("method exit event (String.intern())");
                    ++unfilteredEvents;
                }
                set.resume();
                return false;
            }
        };
        addListener(adapter);
    }
    protected void runTests() throws Exception {
        String[] args = new String[2];
        args[0] = "-connect";
        args[1] = "com.sun.jdi.CommandLineLaunch:main=NativeInstanceFilterTarg";
        connect(args);
        waitForVMStart();
        requestManager = vm().eventRequestManager();
        ReferenceType referenceType =
            resumeToPrepareOf("NativeInstanceFilterTarg").referenceType();
        request = requestManager.createMethodExitRequest();
        request.enable();
        listen();
        vm().resume();
        waitForVMDeath();
        if (unfilteredEvents != 1) {
            throw new Exception(
                "Failed: Event from native frame not filtered out.");
        }
        System.out.println("Passed: Event filtered out.");
    }
}

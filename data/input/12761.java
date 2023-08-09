public class FilterNoMatch extends JDIScaffold {
    static boolean listenCalled;
    public static void main(String args[]) throws Exception {
        new FilterNoMatch().startTests();
    }
    public FilterNoMatch() {
        super();
    }
    private void listen() {
        TargetAdapter adapter = new TargetAdapter() {
            EventSet set = null;
            public boolean eventSetReceived(EventSet set) {
                this.set = set;
                return false;
            }
            public boolean stepCompleted(StepEvent event) {
                listenCalled = true;
                System.out.println("listen: line#=" + event.location().lineNumber()
                                   + " event=" + event);
                StepRequest str= (StepRequest)event.request();
                str.disable();
                set.resume();
                return false;
            }
        };
        listenCalled = false;
        addListener(adapter);
    }
    protected void runTests() throws Exception {
        String[] args = new String[2];
        args[0] = "-connect";
        args[1] = "com.sun.jdi.CommandLineLaunch:main=HelloWorld";
        connect(args);
        waitForVMStart();
        EventRequestManager requestManager = vm().eventRequestManager();
        ReferenceType referenceType = resumeToPrepareOf("HelloWorld").referenceType();
        Location location = findLocation(referenceType, 3);
        BreakpointRequest request
            = requestManager.createBreakpointRequest(location);
        request.enable();
        BreakpointEvent event = (BreakpointEvent)waitForRequestedEvent(request);
        requestManager.deleteEventRequest(request);
        StepRequest request1 = requestManager.createStepRequest(event.thread(),
                                  StepRequest.STEP_LINE,StepRequest.STEP_OVER);
        request1.addClassExclusionFilter( "java.*");
        request1.addClassExclusionFilter( "com.*");
        request1.addClassExclusionFilter( "sun.*");
        request1.addClassExclusionFilter("H");
        request1.addClassExclusionFilter("HelloWorl");
        request1.addClassExclusionFilter("HelloWorldx");
        request1.addClassExclusionFilter("xHelloWorld");
        request1.addClassExclusionFilter("*elloWorldx");
        request1.addClassExclusionFilter("*elloWorl");
        request1.addClassExclusionFilter("*xHelloWorld");
        request1.addClassExclusionFilter("elloWorld*");
        request1.addClassExclusionFilter("HelloWorldx*");
        request1.addClassExclusionFilter("xHelloWorld*");
        request1.enable();
        listen();
        vm().resume();
        waitForVMDeath();
        if ( !listenCalled){
            throw new Exception( "Failed: .");
        }
        System.out.println( "Passed: ");
    }
}

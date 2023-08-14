public class RepStep {
    static final String TARGET = "RepStepTarg";
    static final int GRANULARITY = StepRequest.STEP_LINE;
    static final int DEPTH = StepRequest.STEP_INTO;
    static final int SUCCESS_COUNT = 30;
    VirtualMachine vm;
    final EventRequestManager requestManager;
    int stepNum = 0;
    boolean passed = false;
    public static void main(String args[]) throws Exception {
        new RepStep(args);
    }
    RepStep(String args[]) throws Exception {
        if (args.length > 0) {
            attachTarget(args[0]);
        } else {
            launchTarget();
        }
        requestManager = vm.eventRequestManager();
        runTests();
        dieNice();
    }
    private void createStep(ThreadReference thread) {
        final StepRequest sr =
                  requestManager.createStepRequest(thread,
                                                   GRANULARITY,
                                                   DEPTH);
        sr.addClassExclusionFilter("java.*");
        sr.addClassExclusionFilter("sun.*");
        sr.addClassExclusionFilter("com.sun.*");
        sr.enable();
    }
    private void runTests() throws Exception {
        ThreadReference thread = null;
        EventQueue queue = vm.eventQueue();
        while (true) {
            EventSet set = queue.remove();
            for (EventIterator it = set.eventIterator(); it.hasNext(); ) {
                Event event = it.nextEvent();
                if (event instanceof VMStartEvent) {
                    thread = ((VMStartEvent)event).thread();
                    ClassPrepareRequest cpReq
                        = requestManager.createClassPrepareRequest();
                    cpReq.addClassFilter(TARGET);
                    cpReq.enable();
                } else if (event instanceof ClassPrepareEvent) {
                    createStep(thread);
                    event.request().disable();
                } else if (event instanceof StepEvent) {
                    System.out.println(++stepNum);
                    if (stepNum >= SUCCESS_COUNT) {
                        System.out.println("RepStep passed");
                        event.request().disable();
                        set.resume();
                        return; 
                    }
                } else if (event instanceof VMDeathEvent) {
                    throw new Exception("RepStep failed: steps missed");
                } else {
                    throw new Exception("Unexpected event: " + event);
                }
            }
            set.resume();
       }
    }
    private void dieNice() throws Exception {
        EventQueue queue = vm.eventQueue();
        while (true) {
            EventSet set = queue.remove();
            for (EventIterator it = set.eventIterator(); it.hasNext(); ) {
                Event event = it.nextEvent();
                if (event instanceof VMDeathEvent) {
                } else if (event instanceof VMDisconnectEvent) {
                    set.resume();
                    return; 
                } else {
                    throw new Exception("Unexpected event: " + event);
                }
            }
            set.resume();
       }
    }
    private Connector findConnector(String name) throws Exception {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        throw new Exception("No connector: " + name);
    }
    private void launchTarget() throws Exception {
        LaunchingConnector launcher =
          (LaunchingConnector)findConnector("com.sun.jdi.CommandLineLaunch");
        Map connectorArgs = launcher.defaultArguments();
        Connector.Argument mainArg =
            (Connector.Argument)connectorArgs.get("main");
        mainArg.setValue(TARGET);
        Connector.Argument optionsArg =
            (Connector.Argument)connectorArgs.get("options");
        optionsArg.setValue(VMConnection.getDebuggeeVMOptions());
        vm = launcher.launch(connectorArgs);
        System.out.println("launched: " + TARGET);
    }
    private void attachTarget(String portNum) throws Exception {
        AttachingConnector conn =
            (AttachingConnector)findConnector("com.sun.jdi.SocketAttach");
        Map connectorArgs = conn.defaultArguments();
        Connector.Argument portArg =
            (Connector.Argument)connectorArgs.get("port");
        portArg.setValue(portNum);
        vm = conn.attach(connectorArgs);
        System.out.println("attached to: " + portNum);
    }
}

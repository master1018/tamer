class EventQueueDisconnectTarg {
    public static void main(String args[]) {
        for (int i=0; i < 10; ++i) {
            Say(i);
        }
    }
    static void Say(int what) {
        System.out.println("Say " + what);
    }
}
public class EventQueueDisconnectTest {
    public static void main(String args[]) throws Exception {
        VMConnection connection = new VMConnection(
                                       "com.sun.jdi.CommandLineLaunch:",
                                       VirtualMachine.TRACE_NONE);
        connection.setConnectorArg("main", "EventQueueDisconnectTarg");
        String debuggeeVMOptions = connection.getDebuggeeVMOptions();
        if (!debuggeeVMOptions.equals("")) {
            if (connection.connectorArg("options").length() > 0) {
                throw new IllegalArgumentException("VM options in two places");
            }
            connection.setConnectorArg("options", debuggeeVMOptions);
        }
        VirtualMachine vm = connection.open();
        EventRequestManager requestManager = vm.eventRequestManager();
        MethodEntryRequest req = requestManager.createMethodEntryRequest();
        req.addClassFilter("EventQueueDisconnectTarg");
        req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        req.enable();
        VMDeathRequest ourVMDeathRequest = requestManager.createVMDeathRequest();
        ourVMDeathRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        ourVMDeathRequest.enable();
        vm.resume();
        while (true) {
            EventSet set = vm.eventQueue().remove();
            Event event = set.eventIterator().nextEvent();
            System.err.println("EventSet with: " + event.getClass());
            if (event instanceof VMDisconnectEvent) {
                System.err.println("Disconnecting successfully");
                break;
            }
            if (event instanceof VMDeathEvent) {
                System.err.println("Pausing after VM death");
                try {
                    Thread.sleep(40 * 1000);
                } catch (InterruptedException exc) {
                }
            }
            set.resume();
        }
        System.err.println("EventQueueDisconnectTest passed");
    }
}

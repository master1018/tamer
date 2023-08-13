public class ProcessAttachDebugger {
    public static void main(String main_args[]) throws Exception {
        String pid = main_args[0];
        List<AttachingConnector> l =
            Bootstrap.virtualMachineManager().attachingConnectors();
        AttachingConnector ac = null;
        for (AttachingConnector c: l) {
            if (c.name().equals("com.sun.jdi.ProcessAttach")) {
                ac = c;
                break;
            }
        }
        if (ac == null) {
            throw new RuntimeException("Unable to locate ProcessAttachingConnector");
        }
        Map<String,Connector.Argument> args = ac.defaultArguments();
        Connector.StringArgument arg = (Connector.StringArgument)args.get("pid");
        arg.setValue(pid);
        System.out.println("Debugger is attaching to: " + pid + " ...");
        VirtualMachine vm = ac.attach(args);
        System.out.println("Attached! Now listing threads ...");
        for (ThreadReference thr: vm.allThreads()) {
            System.out.println(thr);
        }
        System.out.println("Debugger done.");
    }
}

public class DebugUsingCustomConnector {
    static Connector find(List l, String name) {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Connector c = (Connector)i.next();
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }
    public static void main(String main_args[]) throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        if (vmm.getClass().getClassLoader() == null) {
            System.out.println("JDI on bootclasspath - test skipped");
            return;
        }
        List launchers = vmm.launchingConnectors();
        LaunchingConnector connector =
            (LaunchingConnector)find(launchers, "SimpleLaunchingConnector");
        Map args = connector.defaultArguments();
        Connector.StringArgument arg =
            (Connector.StringArgument)args.get("class");
        arg.setValue("Foo");
        VirtualMachine vm = connector.launch(args);
        vm.resume();
    }
}

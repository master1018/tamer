public class BasicTests {
    public static void main(String args[]) throws Exception {
        String pid = args[0];
        String agent = args[1];
        String badagent = args[2];
        String redefineagent = args[3];
        System.out.println(" - Attaching to application ...");
        VirtualMachine vm = VirtualMachine.attach(pid);
        System.out.println(" - Test: system properties in target VM");
        Properties props = vm.getSystemProperties();
        String value = props.getProperty("attach.test");
        if (value == null || !value.equals("true")) {
            throw new RuntimeException("attach.test property not set");
        }
        System.out.println(" - attach.test property set as expected");
        System.out.println(" - Test: agent properties in target VM");
        props = vm.getAgentProperties();
        if (props == null || props.size() == 0) {
            throw new RuntimeException("Agent properties is empty");
        }
        System.out.println(" - agent properties non-empty as expected");
        System.out.println(" - Test: Load an agent that does not exist");
        try {
            vm.loadAgent("SilverBullet.jar");
        } catch (AgentLoadException x) {
            System.out.println(" - AgentLoadException thrown as expected!");
        }
        System.out.println(" - Test: Load a bad agent");
        System.out.println("INFO: This test will cause error messages "
            + "to appear in the application log about SilverBullet.jar "
            + "not being found and an agent failing to start.");
        try {
            vm.loadAgent(badagent);
            throw new RuntimeException(
                "AgentInitializationException not thrown as expected!");
        } catch (AgentInitializationException x) {
            System.out.println(
                " - AgentInitializationException thrown as expected!");
        }
        System.out.println(" - Test: Detach from VM");
        System.out.println("INFO: This test will cause error messages "
            + "to appear in the application log about a BadAgent including "
            + "a RuntimeException and an InvocationTargetException.");
        vm.detach();
        try {
            vm.loadAgent(agent);
            throw new RuntimeException("loadAgent did not throw an exception!!");
        } catch (IOException ioe) {
            System.out.println(" - IOException as expected");
        }
        System.out.println(" - Re-attaching to application ...");
        vm = VirtualMachine.attach(pid);
        System.out.println(" - Test: End-to-end connection with agent");
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        System.out.println(" - Loading Agent.jar into target VM ...");
        vm.loadAgent(agent, Integer.toString(port));
        System.out.println(" - Waiting for agent to connect back to tool ...");
        Socket s = ss.accept();
        System.out.println(" - Connected to agent.");
        System.out.println(" - Re-attaching to application ...");
        vm = VirtualMachine.attach(pid);
        System.out.println(" - Test: End-to-end connection with RedefineAgent");
        ServerSocket ss2 = new ServerSocket(0);
        int port2 = ss2.getLocalPort();
        System.out.println(" - Loading RedefineAgent.jar into target VM ...");
        vm.loadAgent(redefineagent, Integer.toString(port2));
        System.out.println(" - Waiting for RedefineAgent to connect back to tool ...");
        Socket s2 = ss2.accept();
        System.out.println(" - Connected to RedefineAgent.");
        System.out.println(" - Test: VirtualMachine.list");
        List<VirtualMachineDescriptor> l = VirtualMachine.list();
        if (!l.isEmpty()) {
            boolean found = false;
            for (VirtualMachineDescriptor vmd: l) {
                if (vmd.id().equals(pid)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                System.out.println(" - " + pid + " found.");
            } else {
                throw new RuntimeException(pid + " not found in VM list");
            }
        }
        System.out.println(" - Test: hashCode/equals");
        VirtualMachine vm1 = VirtualMachine.attach(pid);
        VirtualMachine vm2 = VirtualMachine.attach(pid);
        if (!vm1.equals(vm2)) {
            throw new RuntimeException("virtual machines are not equal");
        }
        if (vm.hashCode() != vm.hashCode()) {
            throw new RuntimeException("virtual machine hashCodes not equal");
        }
        System.out.println(" - hashCode/equals okay");
        System.out.println(" - Cleaning up...");
        s.close();
        ss.close();
        s2.close();
        ss2.close();
    }
}

public class LocalVirtualMachine {
    private String address;
    private String commandLine;
    private String displayName;
    private int vmid;
    private boolean isAttachSupported;
    public LocalVirtualMachine(int vmid, String commandLine, boolean canAttach, String connectorAddress) {
        this.vmid = vmid;
        this.commandLine = commandLine;
        this.address = connectorAddress;
        this.isAttachSupported = canAttach;
        this.displayName = getDisplayName(commandLine);
    }
    private static String getDisplayName(String commandLine) {
        String[] res = commandLine.split(" ", 2);
        if (res[0].endsWith(".jar")) {
           File jarfile = new File(res[0]);
           String displayName = jarfile.getName();
           if (res.length == 2) {
               displayName += " " + res[1];
           }
           return displayName;
        }
        return commandLine;
    }
    public int vmid() {
        return vmid;
    }
    public boolean isManageable() {
        return (address != null);
    }
    public boolean isAttachable() {
        return isAttachSupported;
    }
    public void startManagementAgent() throws IOException {
        if (address != null) {
            return;
        }
        if (!isAttachable()) {
            throw new IOException("This virtual machine \"" + vmid +
                "\" does not support dynamic attach.");
        }
        loadManagementAgent();
        if (address == null) {
            throw new IOException("Fails to find connector address");
        }
    }
    public String connectorAddress() {
        return address;
    }
    public String displayName() {
        return displayName;
    }
    public String toString() {
        return commandLine;
    }
    public static Map<Integer, LocalVirtualMachine> getAllVirtualMachines() {
        Map<Integer, LocalVirtualMachine> map =
            new HashMap<Integer, LocalVirtualMachine>();
        getMonitoredVMs(map);
        getAttachableVMs(map);
        return map;
    }
    private static void getMonitoredVMs(Map<Integer, LocalVirtualMachine> map) {
        MonitoredHost host;
        Set vms;
        try {
            host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
            vms = host.activeVms();
        } catch (java.net.URISyntaxException sx) {
            throw new InternalError(sx.getMessage());
        } catch (MonitorException mx) {
            throw new InternalError(mx.getMessage());
        }
        for (Object vmid: vms) {
            if (vmid instanceof Integer) {
                int pid = ((Integer) vmid).intValue();
                String name = vmid.toString(); 
                boolean attachable = false;
                String address = null;
                try {
                     MonitoredVm mvm = host.getMonitoredVm(new VmIdentifier(name));
                     name =  MonitoredVmUtil.commandLine(mvm);
                     attachable = MonitoredVmUtil.isAttachable(mvm);
                     address = ConnectorAddressLink.importFrom(pid);
                     mvm.detach();
                } catch (Exception x) {
                }
                map.put((Integer) vmid,
                        new LocalVirtualMachine(pid, name, attachable, address));
            }
        }
    }
    private static final String LOCAL_CONNECTOR_ADDRESS_PROP =
        "com.sun.management.jmxremote.localConnectorAddress";
    private static void getAttachableVMs(Map<Integer, LocalVirtualMachine> map) {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vms) {
            try {
                Integer vmid = Integer.valueOf(vmd.id());
                if (!map.containsKey(vmid)) {
                    boolean attachable = false;
                    String address = null;
                    try {
                        VirtualMachine vm = VirtualMachine.attach(vmd);
                        attachable = true;
                        Properties agentProps = vm.getAgentProperties();
                        address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
                        vm.detach();
                    } catch (AttachNotSupportedException x) {
                    } catch (IOException x) {
                    }
                    map.put(vmid, new LocalVirtualMachine(vmid.intValue(),
                                                          vmd.displayName(),
                                                          attachable,
                                                          address));
                }
            } catch (NumberFormatException e) {
            }
        }
    }
    public static LocalVirtualMachine getLocalVirtualMachine(int vmid) {
        Map<Integer, LocalVirtualMachine> map = getAllVirtualMachines();
        LocalVirtualMachine lvm = map.get(vmid);
        if (lvm == null) {
            boolean attachable = false;
            String address = null;
            String name = String.valueOf(vmid); 
            try {
                VirtualMachine vm = VirtualMachine.attach(name);
                attachable = true;
                Properties agentProps = vm.getAgentProperties();
                address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
                vm.detach();
                lvm = new LocalVirtualMachine(vmid, name, attachable, address);
            } catch (AttachNotSupportedException x) {
                if (JConsole.isDebug()) {
                    x.printStackTrace();
                }
            } catch (IOException x) {
                if (JConsole.isDebug()) {
                    x.printStackTrace();
                }
            }
        }
        return lvm;
    }
    private void loadManagementAgent() throws IOException {
        VirtualMachine vm = null;
        String name = String.valueOf(vmid);
        try {
            vm = VirtualMachine.attach(name);
        } catch (AttachNotSupportedException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        }
        String home = vm.getSystemProperties().getProperty("java.home");
        String agent = home + File.separator + "jre" + File.separator +
                           "lib" + File.separator + "management-agent.jar";
        File f = new File(agent);
        if (!f.exists()) {
            agent = home + File.separator +  "lib" + File.separator +
                        "management-agent.jar";
            f = new File(agent);
            if (!f.exists()) {
                throw new IOException("Management agent not found");
            }
        }
        agent = f.getCanonicalPath();
        try {
            vm.loadAgent(agent, "com.sun.management.jmxremote");
        } catch (AgentLoadException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        } catch (AgentInitializationException x) {
            IOException ioe = new IOException(x.getMessage());
            ioe.initCause(x);
            throw ioe;
        }
        Properties agentProps = vm.getAgentProperties();
        address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
        vm.detach();
    }
}

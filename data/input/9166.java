public abstract class HotSpotAttachProvider extends AttachProvider {
    private static final String JVM_VERSION = "java.property.java.vm.version";
    public HotSpotAttachProvider() {
    }
    public void checkAttachPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                new AttachPermission("attachVirtualMachine")
            );
        }
    }
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        ArrayList<VirtualMachineDescriptor> result =
            new ArrayList<VirtualMachineDescriptor>();
        MonitoredHost host;
        Set vms;
        try {
            host = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null));
            vms = host.activeVms();
        } catch (Throwable t) {
            if (t instanceof ExceptionInInitializerError) {
                t = t.getCause();
            }
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath)t;
            }
            if (t instanceof SecurityException) {
                return result;
            }
            throw new InternalError();          
        }
        for (Object vmid: vms) {
            if (vmid instanceof Integer) {
                String pid = vmid.toString();
                String name = pid;      
                boolean isAttachable = false;
                MonitoredVm mvm = null;
                try {
                    mvm = host.getMonitoredVm(new VmIdentifier(pid));
                    try {
                        isAttachable = MonitoredVmUtil.isAttachable(mvm);
                        name =  MonitoredVmUtil.commandLine(mvm);
                    } catch (Exception e) {
                    }
                    if (isAttachable) {
                        result.add(new HotSpotVirtualMachineDescriptor(this, pid, name));
                    }
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath)t;
                    }
                } finally {
                    if (mvm != null) {
                        mvm.detach();
                    }
                }
            }
        }
        return result;
    }
    void testAttachable(String id) throws AttachNotSupportedException {
        MonitoredVm mvm = null;
        try {
            VmIdentifier vmid = new VmIdentifier(id);
            MonitoredHost host = MonitoredHost.getMonitoredHost(vmid);
            mvm = host.getMonitoredVm(vmid);
            if (MonitoredVmUtil.isAttachable(mvm)) {
                return;
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath td = (ThreadDeath)t;
                throw td;
            }
            return;
        } finally {
            if (mvm != null) {
                mvm.detach();
            }
        }
        throw new AttachNotSupportedException(
                  "The VM does not support the attach mechanism");
    }
    static class HotSpotVirtualMachineDescriptor extends VirtualMachineDescriptor {
        HotSpotVirtualMachineDescriptor(AttachProvider provider,
                                        String id,
                                        String displayName) {
            super(provider, id, displayName);
        }
        public boolean isAttachable() {
            return true;
        }
    }
}

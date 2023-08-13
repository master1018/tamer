public class LinuxAttachProvider extends HotSpotAttachProvider {
    private static final String JVM_VERSION = "java.property.java.vm.version";
    public LinuxAttachProvider() {
    }
    public String name() {
        return "sun";
    }
    public String type() {
        return "socket";
    }
    public VirtualMachine attachVirtualMachine(String vmid)
        throws AttachNotSupportedException, IOException
    {
        checkAttachPermission();
        testAttachable(vmid);
        return new LinuxVirtualMachine(this, vmid);
    }
    public VirtualMachine attachVirtualMachine(VirtualMachineDescriptor vmd)
        throws AttachNotSupportedException, IOException
    {
        if (vmd.provider() != this) {
            throw new AttachNotSupportedException("provider mismatch");
        }
        if (vmd instanceof HotSpotVirtualMachineDescriptor) {
            assert ((HotSpotVirtualMachineDescriptor)vmd).isAttachable();
            checkAttachPermission();
            return new LinuxVirtualMachine(this, vmd.id());
        } else {
            return attachVirtualMachine(vmd.id());
        }
    }
}

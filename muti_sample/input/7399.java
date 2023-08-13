public class SolarisAttachProvider extends HotSpotAttachProvider {
    public SolarisAttachProvider() {
    }
    public String name() {
        return "sun";
    }
    public String type() {
        return "doors";
    }
    public VirtualMachine attachVirtualMachine(String vmid)
        throws AttachNotSupportedException, IOException
    {
        checkAttachPermission();
        testAttachable(vmid);
        return new SolarisVirtualMachine(this, vmid);
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
            return new SolarisVirtualMachine(this, vmd.id());
        } else {
            return attachVirtualMachine(vmd.id());
        }
    }
}

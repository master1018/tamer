abstract class MirrorImpl extends Object implements Mirror {
    protected VirtualMachineImpl vm;
    MirrorImpl(VirtualMachine aVm) {
        super();
        vm = (VirtualMachineImpl)aVm;
    }
    public VirtualMachine virtualMachine() {
        return vm;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Mirror)) {
            Mirror other = (Mirror)obj;
            return vm.equals(other.virtualMachine());
        } else {
            return false;
        }
    }
    public int hashCode() {
        return vm.hashCode();
    }
}

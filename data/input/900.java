public class RemoteVmImpl implements RemoteVm {
    private BufferedMonitoredVm mvm;
    RemoteVmImpl(BufferedMonitoredVm mvm) {
        this.mvm = mvm;
    }
    public byte[] getBytes() {
        return mvm.getBytes();
    }
    public int getCapacity() {
        return mvm.getCapacity();
    }
    public void detach() {
        mvm.detach();
    }
    public int getLocalVmId() {
        return mvm.getVmIdentifier().getLocalVmId();
    }
}

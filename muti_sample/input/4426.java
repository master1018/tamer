public class PerfDataBuffer extends AbstractPerfDataBuffer {
    private RemoteVm rvm;
    public PerfDataBuffer(RemoteVm rvm, int lvmid) throws MonitorException {
        this.rvm = rvm;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(rvm.getCapacity());
            sample(buffer);
            createPerfDataBuffer(buffer, lvmid);
        } catch (RemoteException e) {
            throw new MonitorException("Could not read data for remote JVM "
                                       + lvmid, e);
        }
    }
    public void sample(ByteBuffer buffer) throws RemoteException {
        assert buffer != null;
        assert rvm != null;
        synchronized(buffer) {
            buffer.clear();
            buffer.put(rvm.getBytes());
        }
    }
}

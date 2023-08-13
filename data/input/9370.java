public class RemoteMonitoredVm extends AbstractMonitoredVm {
    private ArrayList<VmListener> listeners;
    private NotifierTask notifierTask;
    private SamplerTask samplerTask;
    private Timer timer;
    private RemoteVm rvm;
    private ByteBuffer updateBuffer;
    public RemoteMonitoredVm(RemoteVm rvm, VmIdentifier vmid,
                             Timer timer, int interval)
           throws MonitorException {
        super(vmid, interval);
        this.rvm = rvm;
        pdb = new PerfDataBuffer(rvm, vmid.getLocalVmId());
        this.listeners = new ArrayList<VmListener>();
        this.timer = timer;
    }
    public void attach() throws MonitorException {
        updateBuffer = pdb.getByteBuffer().duplicate();
        if (interval > 0) {
            samplerTask = new SamplerTask();
            timer.schedule(samplerTask, 0, interval);
        }
    }
    public void detach() {
        try {
            if (interval > 0) {
                if (samplerTask != null) {
                    samplerTask.cancel();
                    samplerTask = null;
                }
                if (notifierTask != null) {
                    notifierTask.cancel();
                    notifierTask = null;
                }
                sample();
            }
        } catch (RemoteException e) {
            System.err.println("Could not read data for remote JVM " + vmid);
            e.printStackTrace();
        } finally {
            super.detach();
        }
    }
    public void sample() throws RemoteException {
        assert updateBuffer != null;
        ((PerfDataBuffer)pdb).sample(updateBuffer);
    }
    public RemoteVm getRemoteVm() {
        return rvm;
    }
    public void addVmListener(VmListener l) {
        synchronized(listeners) {
            listeners.add(l);
            if (notifierTask == null) {
                notifierTask = new NotifierTask();
                timer.schedule(notifierTask, 0, interval);
            }
        }
    }
    public void removeVmListener(VmListener l) {
        synchronized(listeners) {
            listeners.remove(l);
            if (listeners.isEmpty() && (notifierTask != null)) {
                notifierTask.cancel();
                notifierTask = null;
            }
        }
    }
    public void setInterval(int newInterval) {
        synchronized(listeners) {
            if (newInterval == interval) {
                return;
            }
            int oldInterval = interval;
            super.setInterval(newInterval);
            if (samplerTask != null) {
                samplerTask.cancel();
                SamplerTask oldSamplerTask = samplerTask;
                samplerTask = new SamplerTask();
                CountedTimerTaskUtils.reschedule(timer, oldSamplerTask,
                                                 samplerTask, oldInterval,
                                                 newInterval);
            }
            if (notifierTask != null) {
                notifierTask.cancel();
                NotifierTask oldNotifierTask = notifierTask;
                notifierTask = new NotifierTask();
                CountedTimerTaskUtils.reschedule(timer, oldNotifierTask,
                                                 notifierTask, oldInterval,
                                                 newInterval);
            }
        }
    }
    void fireMonitorStatusChangedEvents(List inserted, List removed) {
        ArrayList registered = null;
        MonitorStatusChangeEvent ev = null;
        synchronized(listeners) {
            registered = (ArrayList)listeners.clone();
        }
        for (Iterator i = registered.iterator(); i.hasNext(); ) {
            VmListener l = (VmListener)i.next();
            if (ev == null) {
                ev = new MonitorStatusChangeEvent(this, inserted, removed);
            }
            l.monitorStatusChanged(ev);
        }
    }
    void fireMonitorsUpdatedEvents() {
        ArrayList registered = null;
        VmEvent ev = null;
        synchronized(listeners) {
            registered = (ArrayList)listeners.clone();
        }
        for (Iterator i = registered.iterator(); i.hasNext(); ) {
            VmListener l = (VmListener)i.next();
            if (ev == null) {
                ev = new VmEvent(this);
            }
            l.monitorsUpdated(ev);
        }
    }
    private class NotifierTask extends CountedTimerTask {
        public void run() {
            super.run();
            try {
                MonitorStatus status = getMonitorStatus();
                List inserted = status.getInserted();
                List removed = status.getRemoved();
                if (!inserted.isEmpty() || !removed.isEmpty()) {
                    fireMonitorStatusChangedEvents(inserted, removed);
                }
            } catch (MonitorException e) {
                System.err.println("Exception updating monitors for "
                                   + getVmIdentifier());
                e.printStackTrace();
            }
        }
    }
    private class SamplerTask extends CountedTimerTask {
        public void run() {
            super.run();
            try {
                sample();
                fireMonitorsUpdatedEvents();
            } catch (RemoteException e) {
                System.err.println("Exception taking sample for "
                                   + getVmIdentifier());
                e.printStackTrace();
                this.cancel();
            }
        }
    }
}

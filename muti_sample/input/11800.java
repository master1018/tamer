public class LocalMonitoredVm extends AbstractMonitoredVm {
    private ArrayList<VmListener> listeners;
    private NotifierTask task;
    public LocalMonitoredVm(VmIdentifier vmid, int interval)
           throws MonitorException {
        super(vmid, interval);
        this.pdb = new PerfDataBuffer(vmid);
        listeners = new ArrayList<VmListener>();
    }
    public void detach() {
        if (interval > 0) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
        super.detach();
    }
    public void addVmListener(VmListener l) {
        synchronized(listeners) {
            listeners.add(l);
            if (task == null) {
                task = new NotifierTask();
                LocalEventTimer timer = LocalEventTimer.getInstance();
                timer.schedule(task, interval, interval);
            }
        }
    }
    public void removeVmListener(VmListener l) {
        synchronized(listeners) {
            listeners.remove(l);
            if (listeners.isEmpty() && task != null) {
                task.cancel();
                task = null;
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
            if (task != null) {
                task.cancel();
                NotifierTask oldTask = task;
                task = new NotifierTask();
                LocalEventTimer timer = LocalEventTimer.getInstance();
                CountedTimerTaskUtils.reschedule(timer, oldTask, task,
                                                 oldInterval, newInterval);
            }
        }
    }
    void fireMonitorStatusChangedEvents(List inserted, List removed) {
        MonitorStatusChangeEvent ev = null;
        ArrayList registered = null;
        synchronized (listeners) {
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
        VmEvent ev = null;
        ArrayList<VmListener> registered = null;
        synchronized (listeners) {
            registered = cast(listeners.clone());
        }
        for (VmListener l :  registered) {
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
                fireMonitorsUpdatedEvents();
            } catch (MonitorException e) {
                System.err.println("Exception updating monitors for "
                                   + getVmIdentifier());
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("unchecked")
    static <T> T cast(Object x) {
        return (T) x;
    }
}

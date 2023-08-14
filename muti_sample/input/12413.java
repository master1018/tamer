public class MonitoredHostProvider extends MonitoredHost {
    private static final String serverName = "/JStatRemoteHost";
    private static final int DEFAULT_POLLING_INTERVAL = 1000;
    private ArrayList<HostListener> listeners;
    private NotifierTask task;
    private HashSet<Integer> activeVms;
    private RemoteVmManager vmManager;
    private RemoteHost remoteHost;
    private Timer timer;
    public MonitoredHostProvider(HostIdentifier hostId)
           throws MonitorException {
        this.hostId = hostId;
        this.listeners = new ArrayList<HostListener>();
        this.interval = DEFAULT_POLLING_INTERVAL;
        this.activeVms = new HashSet<Integer>();
        String rmiName;
        String sn = serverName;
        String path = hostId.getPath();
        if ((path != null) && (path.length() > 0)) {
            sn = path;
        }
        if (hostId.getPort() != -1) {
            rmiName = "rmi:
        } else {
            rmiName = "rmi:
        }
        try {
            remoteHost = (RemoteHost)Naming.lookup(rmiName);
        } catch (RemoteException e) {
            String message = "RMI Registry not available at "
                             + hostId.getHost();
            if (hostId.getPort() == -1) {
                message = message + ":"
                          + java.rmi.registry.Registry.REGISTRY_PORT;
            } else {
                message = message + ":" + hostId.getPort();
            }
            if (e.getMessage() != null) {
                throw new MonitorException(message + "\n" + e.getMessage(), e);
            } else {
                throw new MonitorException(message, e);
            }
        } catch (NotBoundException e) {
            String message = e.getMessage();
            if (message == null) message = rmiName;
            throw new MonitorException("RMI Server " + message
                                       + " not available", e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Malformed URL: " + rmiName);
        }
        this.vmManager = new RemoteVmManager(remoteHost);
        this.timer = new Timer(true);
    }
    public MonitoredVm getMonitoredVm(VmIdentifier vmid)
                       throws MonitorException {
        return getMonitoredVm(vmid, DEFAULT_POLLING_INTERVAL);
    }
    public MonitoredVm getMonitoredVm(VmIdentifier vmid, int interval)
                       throws MonitorException {
        VmIdentifier nvmid = null;
        try {
            nvmid = hostId.resolve(vmid);
            RemoteVm rvm = remoteHost.attachVm(vmid.getLocalVmId(),
                                               vmid.getMode());
            RemoteMonitoredVm rmvm = new RemoteMonitoredVm(rvm, nvmid, timer,
                                                           interval);
            rmvm.attach();
            return rmvm;
        } catch (RemoteException e) {
            throw new MonitorException("Remote Exception attaching to "
                                       + nvmid.toString(), e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URI: "
                                               + vmid.toString(), e);
        }
    }
    public void detach(MonitoredVm vm) throws MonitorException {
        RemoteMonitoredVm rmvm = (RemoteMonitoredVm)vm;
        rmvm.detach();
        try {
            remoteHost.detachVm(rmvm.getRemoteVm());
        } catch (RemoteException e) {
            throw new MonitorException("Remote Exception detaching from "
                                       + vm.getVmIdentifier().toString(), e);
        }
    }
    public void addHostListener(HostListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
            if (task == null) {
                task = new NotifierTask();
                timer.schedule(task, 0, interval);
            }
        }
    }
    public void removeHostListener(HostListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
            if (listeners.isEmpty() && (task != null)) {
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
                CountedTimerTaskUtils.reschedule(timer, oldTask, task,
                                                 oldInterval, newInterval);
            }
        }
    }
    public Set<Integer> activeVms() throws MonitorException {
        return vmManager.activeVms();
    }
    private void fireVmStatusChangedEvents(Set active, Set started,
                                           Set terminated) {
        ArrayList registered = null;
        VmStatusChangeEvent ev = null;
        synchronized(listeners) {
            registered = (ArrayList)listeners.clone();
        }
        for (Iterator i = registered.iterator(); i.hasNext(); ) {
            HostListener l = (HostListener)i.next();
            if (ev == null) {
                ev = new VmStatusChangeEvent(this, active, started, terminated);
            }
            l.vmStatusChanged(ev);
        }
    }
    void fireDisconnectedEvents() {
        ArrayList registered = null;
        HostEvent ev = null;
        synchronized(listeners) {
            registered = (ArrayList)listeners.clone();
        }
        for (Iterator i = registered.iterator(); i.hasNext(); ) {
            HostListener l = (HostListener)i.next();
            if (ev == null) {
                ev = new HostEvent(this);
            }
            l.disconnected(ev);
        }
    }
    private class NotifierTask extends CountedTimerTask {
        public void run() {
            super.run();
            Set lastActiveVms = activeVms;
            try {
                activeVms = (HashSet<Integer>)vmManager.activeVms();
            } catch (MonitorException e) {
                System.err.println("MonitoredHostProvider: polling task "
                                   + "caught MonitorException:");
                e.printStackTrace();
                setLastException(e);
                fireDisconnectedEvents();
            }
            if (activeVms.isEmpty()) {
                return;
            }
            Set<Integer> startedVms = new HashSet<Integer>();
            Set<Object> terminatedVms = new HashSet<Object>();
            for (Iterator i = activeVms.iterator(); i.hasNext();  ) {
                Integer vmid = (Integer)i.next();
                if (!lastActiveVms.contains(vmid)) {
                    startedVms.add(vmid);
                }
            }
            for (Iterator i = lastActiveVms.iterator(); i.hasNext();
                     ) {
                Object o = i.next();
                if (!activeVms.contains(o)) {
                    terminatedVms.add(o);
                }
            }
            if (!startedVms.isEmpty() || !terminatedVms.isEmpty()) {
                fireVmStatusChangedEvents(activeVms, startedVms, terminatedVms);
            }
        }
    }
}

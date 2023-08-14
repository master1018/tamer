public abstract class MonitoredHost {
    private static Map<HostIdentifier, MonitoredHost> monitoredHosts =
                new HashMap<HostIdentifier, MonitoredHost>();
    private static final String IMPL_OVERRIDE_PROP_NAME =
            "sun.jvmstat.monitor.MonitoredHost";
    private static final String IMPL_PKG_PROP_NAME =
            "sun.jvmstat.monitor.package";
    private static final String IMPL_PACKAGE =
            System.getProperty(IMPL_PKG_PROP_NAME, "sun.jvmstat.perfdata");
    private static final String LOCAL_PROTOCOL_PROP_NAME =
            "sun.jvmstat.monitor.local";
    private static final String LOCAL_PROTOCOL =
            System.getProperty(LOCAL_PROTOCOL_PROP_NAME, "local");
    private static final String REMOTE_PROTOCOL_PROP_NAME =
            "sun.jvmstat.monitor.remote";
    private static final String REMOTE_PROTOCOL =
            System.getProperty(REMOTE_PROTOCOL_PROP_NAME, "rmi");
    private static final String MONITORED_HOST_CLASS = "MonitoredHostProvider";
    protected HostIdentifier hostId;
    protected int interval;
    protected Exception lastException;
    public static MonitoredHost getMonitoredHost(String hostIdString)
                  throws MonitorException, URISyntaxException {
        HostIdentifier hostId = new HostIdentifier(hostIdString);
        return getMonitoredHost(hostId);
    }
    public static MonitoredHost getMonitoredHost(VmIdentifier vmid)
                 throws MonitorException {
        HostIdentifier hostId = new HostIdentifier(vmid);
        return getMonitoredHost(hostId);
    }
    public static MonitoredHost getMonitoredHost(HostIdentifier hostId)
                  throws MonitorException {
        String classname = System.getProperty(IMPL_OVERRIDE_PROP_NAME);
        MonitoredHost mh = null;
        synchronized(monitoredHosts) {
            mh = monitoredHosts.get(hostId);
            if (mh != null) {
                if (mh.isErrored()) {
                    monitoredHosts.remove(hostId);
                } else {
                    return mh;
                }
            }
        }
        hostId = resolveHostId(hostId);
        if (classname == null) {
            classname = IMPL_PACKAGE + ".monitor.protocol."
                        + hostId.getScheme() + "." + MONITORED_HOST_CLASS;
        }
        try {
            Class<?> c = Class.forName(classname);
            Constructor cons = c.getConstructor(
                new Class[] { hostId.getClass() }
            );
            mh = (MonitoredHost)cons.newInstance(new Object[] { hostId } );
            synchronized(monitoredHosts) {
                monitoredHosts.put(mh.hostId, mh);
            }
            return mh;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find " + classname
                                               + ": " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                "Expected constructor missing in " + classname + ": "
                + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                "Unexpected constructor access in " + classname + ": "
                + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(classname + "is abstract: "
                                               + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MonitorException) {
                throw (MonitorException)cause;
            }
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    protected static HostIdentifier resolveHostId(HostIdentifier hostId)
                     throws MonitorException {
        String hostname = hostId.getHost();
        String scheme = hostId.getScheme();
        StringBuffer sb = new StringBuffer();
        assert hostname != null;
        if (scheme == null) {
            if (hostname.compareTo("localhost") == 0) {
                scheme = LOCAL_PROTOCOL;
            } else {
                scheme = REMOTE_PROTOCOL;
            }
        }
        sb.append(scheme).append(":").append(hostId.getSchemeSpecificPart());
        String frag = hostId.getFragment();
        if (frag != null) {
            sb.append("#").append(frag);
        }
        try {
            return new HostIdentifier(sb.toString());
        } catch (URISyntaxException e) {
            assert false;
            throw new IllegalArgumentException("Malformed URI created: "
                                               + sb.toString());
        }
    }
    public HostIdentifier getHostIdentifier() {
        return hostId;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public int getInterval() {
        return interval;
    }
    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
    public Exception getLastException() {
        return lastException;
    }
    public void clearLastException() {
        lastException = null;
    }
    public boolean isErrored() {
        return lastException != null;
    }
    public abstract MonitoredVm getMonitoredVm(VmIdentifier id)
                                throws MonitorException;
    public abstract MonitoredVm getMonitoredVm(VmIdentifier id, int interval)
                                throws MonitorException;
    public abstract void detach(MonitoredVm vm) throws MonitorException;
    public abstract void addHostListener(HostListener listener)
                         throws MonitorException;
    public abstract void removeHostListener(HostListener listener)
                         throws MonitorException;
    public abstract Set<Integer> activeVms() throws MonitorException;
}

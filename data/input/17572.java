public class DirectoryScanner implements
        DirectoryScannerMXBean, NotificationEmitter {
    public static final String FILE_MATCHES_NOTIFICATION =
            "com.sun.jmx.examples.scandir.filematch";
    private static final Logger LOG =
            Logger.getLogger(DirectoryScanner.class.getName());
    private volatile ScanState state = STOPPED;
    private final NotificationBroadcasterSupport broadcaster;
    private final File rootFile;
    private final DirectoryScannerConfig config;
    final Set<Action> actions;
    final ResultLogManager logManager;
    public DirectoryScanner(DirectoryScannerConfig config,
                            ResultLogManager logManager)
        throws IllegalArgumentException {
        if (logManager == null)
            throw new IllegalArgumentException("log=null");
        if (config == null)
            throw new IllegalArgumentException("config=null");
        if (config.getName() == null)
            throw new IllegalArgumentException("config.name=null");
         broadcaster = new NotificationBroadcasterSupport();
         this.config = XmlConfigUtils.xmlClone(config);
         rootFile = validateRoot(config.getRootDirectory());
         if (config.getActions() == null)
             actions = Collections.emptySet();
         else
             actions = EnumSet.copyOf(Arrays.asList(config.getActions()));
         this.logManager = logManager;
    }
    public void stop() {
        setStateAndNotify(STOPPED);
    }
    public String getRootDirectory() {
        return rootFile.getAbsolutePath();
    }
    public ScanState getState() {
        return state;
    }
    public DirectoryScannerConfig getConfiguration() {
        return config;
    }
    public String getCurrentScanInfo() {
        final ScanTask currentOrLastTask = currentTask;
        if (currentOrLastTask == null) return "Never Run";
        return currentOrLastTask.getScanInfo();
    }
    private volatile ScanTask currentTask = null;
    public void scan() {
        final ScanTask task;
        synchronized (this) {
            final LinkedList<File> list;
            switch (state) {
                case RUNNING:
                case SCHEDULED:
                    throw new IllegalStateException(state.toString());
                case STOPPED:
                case COMPLETED:
                    list = new LinkedList<File>();
                    list.add(rootFile);
                    break;
                default:
                    throw new IllegalStateException(String.valueOf(state));
            }
            currentTask = task = new ScanTask(list,this);
            setStateAndNotify(SCHEDULED);
        }
        task.execute();
    }
    void actOn(File file) {
        final Set<Action> taken = new HashSet<Action>();
        boolean logresult = false;
        for (Action action : actions) {
            switch (action) {
                case DELETE:
                    if (deleteFile(file)) {
                        taken.add(DELETE);
                    }
                    break;
                case NOTIFY:
                    if (notifyMatch(file)) {
                        taken.add(NOTIFY);
                    }
                    break;
                case LOGRESULT:
                    logresult = true;
                    break;
                default:
                    LOG.fine("Failed to execute action: " +action +
                            " - action not supported");
                    break;
            }
        }
        if (logresult) {
            taken.add(LOGRESULT);
            if (!logResult(file,taken.toArray(new Action[taken.size()])))
                taken.remove(LOGRESULT); 
        }
        LOG.finest("File processed: "+taken+" - "+file.getAbsolutePath());
    }
    private boolean deleteFile(File file) {
        try {
            System.out.println("DELETE not implemented for safety reasons.");
            return true;
        } catch (Exception x) {
            LOG.fine("Failed to delete: "+file.getAbsolutePath());
        }
        return false;
    }
    private boolean notifyMatch(File file) {
        try {
            final Notification n =
                    new Notification(FILE_MATCHES_NOTIFICATION,this,
                    getNextSeqNumber(),
                    file.getAbsolutePath());
            broadcaster.sendNotification(n);
            return true;
        } catch (Exception x) {
            LOG.fine("Failed to notify: "+file.getAbsolutePath());
        }
        return false;
    }
    private boolean logResult(File file,Action[] actions) {
        try {
            logManager.log(new ResultRecord(config, actions,file));
            return true;
        } catch (Exception x) {
            LOG.fine("Failed to log: "+file.getAbsolutePath());
        }
        return false;
    }
    private static class ScanTask {
        private final LinkedList<File>   list;
        private final DirectoryScanner scan;
        private volatile long scanned=0;
        private volatile long matching=0;
        private volatile String info="Not started";
        ScanTask(LinkedList<File> list, DirectoryScanner scan) {
            this.list = list; this.scan = scan;
        }
        public void execute() {
            scan(list);
        }
        private void scan(LinkedList<File> list) {
             scan.scan(this,list);
        }
        public String getScanInfo() {
            return info+" - ["+scanned+" scanned, "+matching+" matching]";
        }
    }
    private void scan(ScanTask task, LinkedList<File> list) {
        setStateAndNotify(RUNNING);
        task.info = "In Progress";
        try {
            final FileFilter filter = config.buildFileFilter();
            while (!list.isEmpty() && state == RUNNING) {
                final File current = list.poll();
                task.scanned++;
                if (current.isFile()) {
                    task.matching++;
                    actOn(current);
                }
                if (current.isDirectory()) {
                    final File[] content = current.listFiles(filter);
                    if (content == null) continue;
                    list.addAll(0,Arrays.asList(content));
                }
            }
            if (list.isEmpty()) {
                task.info = "Successfully Completed";
                setStateAndNotify(COMPLETED);
            }
        } catch (Exception x) {
            task.info = "Failed: "+x;
            if (LOG.isLoggable(Level.FINEST))
                LOG.log(Level.FINEST,"scan task failed: "+x,x);
            else if (LOG.isLoggable(Level.FINE))
                LOG.log(Level.FINE,"scan task failed: "+x);
            setStateAndNotify(STOPPED);
        } catch (Error e) {
            state=STOPPED;
            task.info = "Error: "+e;
            throw e;
        }
    }
    public void addNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback)
            throws IllegalArgumentException {
        broadcaster.addNotificationListener(listener, filter, handback);
    }
    private final void setStateAndNotify(ScanState desired) {
        final ScanState old = state;
        if (old == desired) return;
        state = desired;
        final AttributeChangeNotification n =
                new AttributeChangeNotification(this,
                getNextSeqNumber(),System.currentTimeMillis(),
                "state change","State",ScanState.class.getName(),
                String.valueOf(old),String.valueOf(desired));
        broadcaster.sendNotification(n);
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] {
            new MBeanNotificationInfo(
                    new String[] {FILE_MATCHES_NOTIFICATION},
                    Notification.class.getName(),
                    "Emitted when a file that matches the scan criteria is found"
                    ),
            new MBeanNotificationInfo(
                    new String[] {AttributeChangeNotification.ATTRIBUTE_CHANGE},
                    AttributeChangeNotification.class.getName(),
                    "Emitted when the State attribute changes"
                    )
        };
    }
    public void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener);
    }
    public void removeNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback)
            throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener, filter, handback);
    }
    private static File validateRoot(String root) {
        if (root == null)
            throw new IllegalArgumentException("no root specified");
        if (root.length() == 0)
            throw new IllegalArgumentException("specified root \"\" is invalid");
        final File f = new File(root);
        if (!f.canRead())
            throw new IllegalArgumentException("can't read "+root);
        if (!f.isDirectory())
            throw new IllegalArgumentException("no such directory: "+root);
        return f;
    }
}

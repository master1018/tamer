public class MonitorVmStartTerminate {
    private static final int SLEEPERS = 10;
    private static final int SLEEPTIME = 5000;     
    private static final int EXECINTERVAL = 3000;   
    private static final int JOINTIME = (SLEEPERS * EXECINTERVAL)
                                        + SLEEPTIME * 2;
    public static void main(String args[]) throws Exception {
        long now = System.currentTimeMillis();
        String sleeperArgs = SLEEPTIME + " " + now;
        String sleeperPattern = "Sleeper " + sleeperArgs + " \\d+$";
        MonitoredHost host = MonitoredHost.getMonitoredHost("localhost");
        host.setInterval(200);
        SleeperListener listener = new SleeperListener(host, sleeperPattern);
        host.addHostListener(listener);
        SleeperStarter ss = new SleeperStarter(SLEEPERS, EXECINTERVAL,
                                               sleeperArgs);
        ss.start();
        System.out.println("Waiting for "
                           + SLEEPERS + " sleepers to terminate");
        try {
            ss.join(JOINTIME);
        } catch (InterruptedException e) {
            System.err.println("Timed out waiting for sleepers");
        }
        if (listener.getStarted() != SLEEPERS) {
            throw new RuntimeException(
                    "Too few sleepers started: "
                    + " started = " + listener.getStarted()
                    + " SLEEPERS = " + SLEEPERS);
        }
        if (listener.getStarted() != listener.getTerminated()) {
            throw new RuntimeException(
                    "Started count != terminated count: "
                    + " started = " + listener.getStarted()
                    + " terminated = " + listener.getTerminated());
        }
    }
}
class SleeperListener implements HostListener {
    private static final boolean DEBUG = false;
    int started;
    int terminated;
    MonitoredHost host;
    Matcher patternMatcher;
    ArrayList targets;
    public SleeperListener(MonitoredHost host, String sleeperPattern) {
        this.host = host;
        Pattern pattern = Pattern.compile(sleeperPattern);
        patternMatcher = pattern.matcher("");
        targets = new ArrayList();
    }
    private void printList(Iterator i, String msg) {
        System.out.println(msg + ":");
        while (i.hasNext()) {
            Integer lvmid = (Integer)i.next();
            try {
                VmIdentifier vmid = new VmIdentifier("
                MonitoredVm target = host.getMonitoredVm(vmid);
                StringMonitor cmdMonitor =
                        (StringMonitor)target.findByName("sun.rt.javaCommand");
                String cmd = cmdMonitor.stringValue();
                System.out.println("\t" + lvmid.intValue() + ": "
                                   + "\"" + cmd + "\"" + ": ");
            } catch (URISyntaxException e) {
                System.err.println("Unexpected URISyntaxException: "
                                   + e.getMessage());
            } catch (MonitorException e) {
                System.out.println("\t" + lvmid.intValue()
                                   + ": error reading monitoring data: "
                                   + " target possibly terminated?");
            }
        }
    }
    private int addStarted(Iterator i) {
        int found = 0;
        while (i.hasNext()) {
            try {
                Integer lvmid = (Integer)i.next();
                VmIdentifier vmid = new VmIdentifier("
                MonitoredVm target = host.getMonitoredVm(vmid);
                StringMonitor cmdMonitor =
                        (StringMonitor)target.findByName("sun.rt.javaCommand");
                String cmd = cmdMonitor.stringValue();
                patternMatcher.reset(cmd);
                System.out.print("Started: " + lvmid.intValue()
                                 + ": " + "\"" + cmd + "\"" + ": ");
                if (patternMatcher.matches()) {
                    System.out.println("matches pattern - recorded");
                    targets.add(lvmid);
                    found++;
                }
                else {
                    System.out.println("does not match pattern - ignored");
                }
            } catch (URISyntaxException e) {
                System.err.println("Unexpected URISyntaxException: "
                                   + e.getMessage());
            } catch (MonitorException e) {
                System.err.println("Unexpected MonitorException: "
                                   + e.getMessage());
            }
        }
        return found;
    }
    private int removeTerminated(Iterator i) {
        int found = 0;
        while (i.hasNext()) {
            Integer lvmid = (Integer)i.next();
            System.out.print("Terminated: " + lvmid.intValue() + ": ");
            if (targets.contains(lvmid)) {
                System.out.println("matches pattern - termination recorded");
                targets.remove(lvmid);
                found++;
            }
            else {
                System.out.println("does not match pattern - ignored");
            }
        }
        return found;
    }
    public synchronized int getStarted() {
        return started;
    }
    public synchronized int getTerminated() {
        return terminated;
    }
    public void vmStatusChanged(VmStatusChangeEvent ev) {
        if (DEBUG) {
            printList(ev.getActive().iterator(), "Active");
            printList(ev.getStarted().iterator(), "Started");
            printList(ev.getTerminated().iterator(), "Terminated");
        }
        int recentlyStarted = addStarted(ev.getStarted().iterator());
        int recentlyTerminated = removeTerminated(
                ev.getTerminated().iterator());
        synchronized (this) {
            started += recentlyStarted;
            terminated += recentlyTerminated;
        }
    }
    public void disconnected(HostEvent ev) {
    }
}
class SleeperStarter extends Thread {
    JavaProcess[] processes;
    int execInterval;
    String args;
    public SleeperStarter(int sleepers, int execInterval, String args) {
        this.execInterval = execInterval;
        this.args = args;
        this.processes = new JavaProcess[sleepers];
    }
    private synchronized int active() {
        int active = processes.length;
        for(int i = 0; i < processes.length; i++) {
            try {
                int exitValue = processes[i].exitValue();
                active--;
            } catch (IllegalThreadStateException e) {
            }
        }
        return active;
    }
    public void run() {
       System.out.println("Starting " + processes.length + " sleepers");
       String[] classpath = {
           "-classpath",
           System.getProperty("java.class.path")
       };
       for (int i = 0; i < processes.length; i++) {
           try {
               System.out.println("Starting Sleeper " + i);
               synchronized(this) {
                   processes[i] = new JavaProcess("Sleeper", args + " " + i);
                   processes[i].addOptions(classpath);
               }
               processes[i].start();
               Thread.sleep(execInterval);
           } catch (InterruptedException ignore) {
           } catch (IOException e) {
               System.err.println(
                       "IOException trying to start Sleeper " + i + ": "
                       + e.getMessage());
           }
       }
       while (active() > 0) ;
       try { Thread.sleep(2000); } catch (InterruptedException ignore) { }
    }
}

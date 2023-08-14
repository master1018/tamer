public class DGCAckHandler {
    private static final long dgcAckTimeout =           
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.dgc.ackTimeout", 300000));
    private static final ScheduledExecutorService scheduler =
        AccessController.doPrivileged(
            new RuntimeUtil.GetInstanceAction()).getScheduler();
    private static final Map<UID,DGCAckHandler> idTable =
        Collections.synchronizedMap(new HashMap<UID,DGCAckHandler>());
    private final UID id;
    private List<Object> objList = new ArrayList<Object>(); 
    private Future<?> task = null;
    DGCAckHandler(UID id) {
        this.id = id;
        if (id != null) {
            assert !idTable.containsKey(id);
            idTable.put(id, this);
        }
    }
    synchronized void add(Object obj) {
        if (objList != null) {
            objList.add(obj);
        }
    }
    synchronized void startTimer() {
        if (objList != null && task == null) {
            task = scheduler.schedule(new Runnable() {
                public void run() {
                    release();
                }
            }, dgcAckTimeout, TimeUnit.MILLISECONDS);
        }
    }
    synchronized void release() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        objList = null;
    }
    public static void received(UID id) {
        DGCAckHandler h = idTable.remove(id);
        if (h != null) {
            h.release();
        }
    }
}

public class ThreadPoolImpl implements ThreadPool
{
    private static int threadCounter = 0; 
    private WorkQueue workQueue;
    private int availableWorkerThreads = 0;
    private int currentThreadCount = 0;
    private int minWorkerThreads = 0;
    private int maxWorkerThreads = 0;
    private long inactivityTimeout;
    private boolean boundedThreadPool = false;
    private long processedCount = 1;
    private long totalTimeTaken = 0;
    private Object lock = new Object();
    private String name;
    private MonitoredObject threadpoolMonitoredObject;
    private ThreadGroup threadGroup ;
    public ThreadPoolImpl(ThreadGroup tg, String threadpoolName) {
        inactivityTimeout = ORBConstants.DEFAULT_INACTIVITY_TIMEOUT;
        maxWorkerThreads = Integer.MAX_VALUE;
        workQueue = new WorkQueueImpl(this);
        threadGroup = tg ;
        name = threadpoolName;
        initializeMonitoring();
    }
    public ThreadPoolImpl(String threadpoolName) {
        this( Thread.currentThread().getThreadGroup(), threadpoolName ) ;
    }
    public ThreadPoolImpl(int minSize, int maxSize, long timeout,
                                            String threadpoolName)
    {
        minWorkerThreads = minSize;
        maxWorkerThreads = maxSize;
        inactivityTimeout = timeout;
        boundedThreadPool = true;
        workQueue = new WorkQueueImpl(this);
        name = threadpoolName;
        for (int i = 0; i < minWorkerThreads; i++) {
            createWorkerThread();
        }
        initializeMonitoring();
    }
    private void initializeMonitoring() {
        MonitoredObject root = MonitoringFactories.getMonitoringManagerFactory().
                createMonitoringManager(MonitoringConstants.DEFAULT_MONITORING_ROOT, null).
                getRootMonitoredObject();
        MonitoredObject threadPoolMonitoringObjectRoot = root.getChild(
                    MonitoringConstants.THREADPOOL_MONITORING_ROOT);
        if (threadPoolMonitoringObjectRoot == null) {
            threadPoolMonitoringObjectRoot =  MonitoringFactories.
                    getMonitoredObjectFactory().createMonitoredObject(
                    MonitoringConstants.THREADPOOL_MONITORING_ROOT,
                    MonitoringConstants.THREADPOOL_MONITORING_ROOT_DESCRIPTION);
            root.addChild(threadPoolMonitoringObjectRoot);
        }
        threadpoolMonitoredObject = MonitoringFactories.
                    getMonitoredObjectFactory().
                    createMonitoredObject(name,
                    MonitoringConstants.THREADPOOL_MONITORING_DESCRIPTION);
        threadPoolMonitoringObjectRoot.addChild(threadpoolMonitoredObject);
        LongMonitoredAttributeBase b1 = new
            LongMonitoredAttributeBase(MonitoringConstants.THREADPOOL_CURRENT_NUMBER_OF_THREADS,
                    MonitoringConstants.THREADPOOL_CURRENT_NUMBER_OF_THREADS_DESCRIPTION) {
                public Object getValue() {
                    return new Long(ThreadPoolImpl.this.currentNumberOfThreads());
                }
            };
        threadpoolMonitoredObject.addAttribute(b1);
        LongMonitoredAttributeBase b2 = new
            LongMonitoredAttributeBase(MonitoringConstants.THREADPOOL_NUMBER_OF_AVAILABLE_THREADS,
                    MonitoringConstants.THREADPOOL_CURRENT_NUMBER_OF_THREADS_DESCRIPTION) {
                public Object getValue() {
                    return new Long(ThreadPoolImpl.this.numberOfAvailableThreads());
                }
            };
        threadpoolMonitoredObject.addAttribute(b2);
        LongMonitoredAttributeBase b3 = new
            LongMonitoredAttributeBase(MonitoringConstants.THREADPOOL_NUMBER_OF_BUSY_THREADS,
                    MonitoringConstants.THREADPOOL_NUMBER_OF_BUSY_THREADS_DESCRIPTION) {
                public Object getValue() {
                    return new Long(ThreadPoolImpl.this.numberOfBusyThreads());
                }
            };
        threadpoolMonitoredObject.addAttribute(b3);
        LongMonitoredAttributeBase b4 = new
            LongMonitoredAttributeBase(MonitoringConstants.THREADPOOL_AVERAGE_WORK_COMPLETION_TIME,
                    MonitoringConstants.THREADPOOL_AVERAGE_WORK_COMPLETION_TIME_DESCRIPTION) {
                public Object getValue() {
                    return new Long(ThreadPoolImpl.this.averageWorkCompletionTime());
                }
            };
        threadpoolMonitoredObject.addAttribute(b4);
        LongMonitoredAttributeBase b5 = new
            LongMonitoredAttributeBase(MonitoringConstants.THREADPOOL_CURRENT_PROCESSED_COUNT,
                    MonitoringConstants.THREADPOOL_CURRENT_PROCESSED_COUNT_DESCRIPTION) {
                public Object getValue() {
                    return new Long(ThreadPoolImpl.this.currentProcessedCount());
                }
            };
        threadpoolMonitoredObject.addAttribute(b5);
        threadpoolMonitoredObject.addChild(
                ((WorkQueueImpl)workQueue).getMonitoredObject());
    }
    MonitoredObject getMonitoredObject() {
        return threadpoolMonitoredObject;
    }
    public WorkQueue getAnyWorkQueue()
    {
        return workQueue;
    }
    public WorkQueue getWorkQueue(int queueId)
        throws NoSuchWorkQueueException
    {
        if (queueId != 0)
            throw new NoSuchWorkQueueException();
        return workQueue;
    }
    void notifyForAvailableWork(WorkQueue aWorkQueue) {
        synchronized (lock) {
            if (availableWorkerThreads == 0) {
                createWorkerThread();
            } else {
                aWorkQueue.notify();
            }
        }
    }
    void createWorkerThread() {
        WorkerThread thread;
        synchronized (lock) {
            if (boundedThreadPool) {
                if (currentThreadCount < maxWorkerThreads) {
                    thread = new WorkerThread(threadGroup, getName());
                    currentThreadCount++;
                } else {
                    return;
                }
            } else {
                thread = new WorkerThread(threadGroup, getName());
                currentThreadCount++;
            }
        }
        try {
            thread.setDaemon(true);
        } catch (Exception e) {
        }
        thread.start();
    }
    public int minimumNumberOfThreads() {
        return minWorkerThreads;
    }
    public int maximumNumberOfThreads() {
        return maxWorkerThreads;
    }
    public long idleTimeoutForThreads() {
        return inactivityTimeout;
    }
    public int currentNumberOfThreads() {
        synchronized (lock) {
            return currentThreadCount;
        }
    }
    public int numberOfAvailableThreads() {
        synchronized (lock) {
            return availableWorkerThreads;
        }
    }
    public int numberOfBusyThreads() {
        synchronized (lock) {
            return (currentThreadCount - availableWorkerThreads);
        }
    }
    public long averageWorkCompletionTime() {
        synchronized (lock) {
            return (totalTimeTaken / processedCount);
        }
    }
    public long currentProcessedCount() {
        synchronized (lock) {
            return processedCount;
        }
    }
    public String getName() {
        return name;
    }
    public int numberOfWorkQueues() {
        return 1;
    }
    private static synchronized int getUniqueThreadId() {
        return ThreadPoolImpl.threadCounter++;
    }
    private class WorkerThread extends Thread
    {
        private Work currentWork;
        private int threadId = 0; 
        private String threadPoolName;
        private StringBuffer workerThreadName = new StringBuffer();
        WorkerThread(ThreadGroup tg, String threadPoolName) {
            super(tg, "Idle");
            this.threadId = ThreadPoolImpl.getUniqueThreadId();
            this.threadPoolName = threadPoolName;
            setName(composeWorkerThreadName(threadPoolName, "Idle"));
        }
        public void run() {
            while (true) {
                try {
                    synchronized (lock) {
                        availableWorkerThreads++;
                    }
                    currentWork = ((WorkQueueImpl)workQueue).requestWork(inactivityTimeout);
                    synchronized (lock) {
                        availableWorkerThreads--;
                        if  ((availableWorkerThreads == 0) &&
                                (workQueue.workItemsInQueue() > 0)) {
                            createWorkerThread();
                        }
                    }
                    setName(composeWorkerThreadName(threadPoolName,
                                      Integer.toString(this.threadId)));
                    long start = System.currentTimeMillis();
                    try {
                        currentWork.doWork();
                    } catch (Throwable t) {
                        ;
                    }
                    long end = System.currentTimeMillis();
                    synchronized (lock) {
                        totalTimeTaken += (end - start);
                        processedCount++;
                    }
                    currentWork = null;
                    setName(composeWorkerThreadName(threadPoolName, "Idle"));
                } catch (TimeoutException e) {
                    synchronized (lock) {
                        availableWorkerThreads--;
                        if (currentThreadCount > minWorkerThreads) {
                            currentThreadCount--;
                            return;
                        } else {
                            continue;
                        }
                    }
                } catch (InterruptedException ie) {
                    synchronized (lock) {
                        availableWorkerThreads--;
                    }
                } catch (Throwable e) {
                    synchronized (lock) {
                        availableWorkerThreads--;
                    }
                }
            }
        }
        private String composeWorkerThreadName(String poolName, String workerName) {
            workerThreadName.setLength(0);
            workerThreadName.append("p: ").append(poolName);
            workerThreadName.append("; w: ").append(workerName);
            return workerThreadName.toString();
        }
    } 
}

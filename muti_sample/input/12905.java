public class ThreadPoolManagerImpl implements ThreadPoolManager
{
    private ThreadPool threadPool ;
    public ThreadPoolManagerImpl( ThreadGroup tg )
    {
        threadPool = new ThreadPoolImpl( tg,
            ORBConstants.THREADPOOL_DEFAULT_NAME ) ;
    }
    public ThreadPool getThreadPool(String threadpoolId)
        throws NoSuchThreadPoolException {
        return threadPool;
    }
    public ThreadPool getThreadPool(int numericIdForThreadpool)
        throws NoSuchThreadPoolException {
        return threadPool;
    }
    public int  getThreadPoolNumericId(String threadpoolId) {
        return 0;
    }
    public String getThreadPoolStringId(int numericIdForThreadpool) {
       return "";
    }
    public ThreadPool getDefaultThreadPool() {
        return threadPool;
    }
    public ThreadPoolChooser getThreadPoolChooser(String componentId) {
        return null;
    }
    public ThreadPoolChooser getThreadPoolChooser(int componentIndex) {
        return null;
    }
    public void setThreadPoolChooser(String componentId, ThreadPoolChooser aThreadPoolChooser) {
    }
    public int getThreadPoolChooserNumericId(String componentId) {
        return 0;
    }
}

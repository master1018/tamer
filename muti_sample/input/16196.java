public abstract class EventHandlerBase
    implements
        EventHandler
{
    protected ORB orb;
    protected Work work;
    protected boolean useWorkerThreadForEvent;
    protected boolean useSelectThreadToWait;
    protected SelectionKey selectionKey;
    public void setUseSelectThreadToWait(boolean x)
    {
        useSelectThreadToWait = x;
    }
    public boolean shouldUseSelectThreadToWait()
    {
        return useSelectThreadToWait;
    }
    public void setSelectionKey(SelectionKey selectionKey)
    {
        this.selectionKey = selectionKey;
    }
    public SelectionKey getSelectionKey()
    {
        return selectionKey;
    }
    public void handleEvent()
    {
        if (orb.transportDebugFlag) {
            dprint(".handleEvent->: " + this);
        }
        getSelectionKey().interestOps(getSelectionKey().interestOps() &
                                      (~ getInterestOps()));
        if (shouldUseWorkerThreadForEvent()) {
            Throwable throwable = null;
            try {
                if (orb.transportDebugFlag) {
                    dprint(".handleEvent: addWork to pool: " + 0);
                }
                orb.getThreadPoolManager().getThreadPool(0)
                    .getWorkQueue(0).addWork(getWork());
            } catch (NoSuchThreadPoolException e) {
                throwable = e;
            } catch (NoSuchWorkQueueException e) {
                throwable = e;
            }
            if (throwable != null) {
                if (orb.transportDebugFlag) {
                    dprint(".handleEvent: " + throwable);
                }
                INTERNAL i = new INTERNAL("NoSuchThreadPoolException");
                i.initCause(throwable);
                throw i;
            }
        } else {
            if (orb.transportDebugFlag) {
                dprint(".handleEvent: doWork");
            }
            getWork().doWork();
        }
        if (orb.transportDebugFlag) {
            dprint(".handleEvent<-: " + this);
        }
    }
    public boolean shouldUseWorkerThreadForEvent()
    {
        return useWorkerThreadForEvent;
    }
    public void setUseWorkerThreadForEvent(boolean x)
    {
        useWorkerThreadForEvent = x;
    }
    public void setWork(Work work)
    {
        this.work = work;
    }
    public Work getWork()
    {
        return work;
    }
    private void dprint(String msg)
    {
        ORBUtility.dprint("EventHandlerBase", msg);
    }
}

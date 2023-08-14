public class BasicPooledConnAdapter extends AbstractPooledConnAdapter {
    protected BasicPooledConnAdapter(ThreadSafeClientConnManager tsccm,
                               AbstractPoolEntry entry) {
        super(tsccm, entry);
        markReusable();
    }
    @Override
    protected ClientConnectionManager getManager() {
        return super.getManager();
    }
    protected AbstractPoolEntry getPoolEntry() {
        return super.poolEntry;
    }
    @Override
    protected void detach() {
        super.detach();
    }
}

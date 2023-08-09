public class SyncProgressMonitor implements ISyncProgressMonitor {
    private IProgressMonitor mMonitor;
    private String mName;
    public SyncProgressMonitor(IProgressMonitor monitor, String name) {
        mMonitor = monitor;
        mName = name;
    }
    public void start(int totalWork) {
        mMonitor.beginTask(mName, totalWork);
    }
    public void stop() {
        mMonitor.done();
    }
    public void advance(int work) {
        mMonitor.worked(work);
    }
    public boolean isCanceled() {
        return mMonitor.isCanceled();
    }
    public void startSubTask(String name) {
        mMonitor.subTask(name);
    }
}

public final class ProgressTaskFactory implements ITaskFactory {
    private final Shell mShell;
    public ProgressTaskFactory(Shell shell) {
        mShell = shell;
    }
    public void start(String title, ITask task) {
        new ProgressTask(mShell, title, task);
    }
}

public abstract class BackgroundAction extends AbstractAction {
    protected void executeBackgroundTask(SwingWorker<?, ?> worker) {
        if (worker != null) {
            worker.execute();
        }
    }
}

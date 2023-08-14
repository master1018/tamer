public final class ProgressTask implements ITaskMonitor {
    private static final double MAX_COUNT = 10000.0;
    private final ProgressDialog mDialog;
    private boolean mAutomaticallyCloseOnTaskCompletion = true;
    private double mIncCoef = 0;
    private double mValue = 0;
    public ProgressTask(Shell parent, String title, ITask task) {
        mDialog = new ProgressDialog(parent, createTaskThread(title, task));
        mDialog.setText(title);
        mDialog.open();
    }
    public void setDescription(String descriptionFormat, Object...args) {
        mDialog.setDescription(descriptionFormat, args);
    }
    public void setResult(String resultFormat, Object...args) {
        mAutomaticallyCloseOnTaskCompletion = false;
        mDialog.setResult(resultFormat, args);
    }
    public void setProgressMax(int max) {
        assert max > 0;
        mDialog.setProgressMax((int) MAX_COUNT);
        mIncCoef = max > 0 ? MAX_COUNT / max : 0;
        assert mIncCoef > 0;
    }
    public void incProgress(int delta) {
        assert mIncCoef > 0;
        assert delta > 0;
        internalIncProgress(delta * mIncCoef);
    }
    private void internalIncProgress(double realDelta) {
        mValue += realDelta;
        mDialog.setProgress((int)mValue);
    }
    public int getProgress() {
        assert mIncCoef > 0;
        return mIncCoef > 0 ? (int)(mDialog.getProgress() / mIncCoef) : 0;
    }
    public boolean isCancelRequested() {
        return mDialog.isCancelRequested();
    }
    private Thread createTaskThread(String title, final ITask task) {
        if (task != null) {
            return new Thread(title) {
                @Override
                public void run() {
                    task.run(ProgressTask.this);
                    if (mAutomaticallyCloseOnTaskCompletion) {
                        mDialog.setAutoCloseRequested();
                    } else {
                        mDialog.setManualCloseRequested();
                    }
                }
            };
        }
        return null;
    }
    public boolean displayPrompt(final String title, final String message) {
        final Shell shell = mDialog.getParent();
        Display display = shell.getDisplay();
        final boolean[] result = new boolean[] { false };
        display.syncExec(new Runnable() {
            public void run() {
                result[0] = MessageDialog.openQuestion(shell, title, message);
            }
        });
        return result[0];
    }
    public ITaskMonitor createSubMonitor(int tickCount) {
        assert mIncCoef > 0;
        assert tickCount > 0;
        return new SubTaskMonitor(this, null, mValue, tickCount * mIncCoef);
    }
    private interface ISubTaskMonitor extends ITaskMonitor {
        public void subIncProgress(double realDelta);
    }
    private static class SubTaskMonitor implements ISubTaskMonitor {
        private final ProgressTask mRoot;
        private final ISubTaskMonitor mParent;
        private final double mStart;
        private final double mSpan;
        private double mSubValue;
        private double mSubCoef;
        public SubTaskMonitor(ProgressTask root,
                ISubTaskMonitor parent,
                double start,
                double span) {
            mRoot = root;
            mParent = parent;
            mStart = start;
            mSpan = span;
            mSubValue = start;
        }
        public boolean isCancelRequested() {
            return mRoot.isCancelRequested();
        }
        public void setDescription(String descriptionFormat, Object... args) {
            mRoot.setDescription(descriptionFormat, args);
        }
        public void setResult(String resultFormat, Object... args) {
            mRoot.setResult(resultFormat, args);
        }
        public void setProgressMax(int max) {
            assert max > 0;
            mSubCoef = max > 0 ? mSpan / max : 0;
            assert mSubCoef > 0;
        }
        public int getProgress() {
            assert mSubCoef > 0;
            return mSubCoef > 0 ? (int)((mSubValue - mStart) / mSubCoef) : 0;
        }
        public void incProgress(int delta) {
            assert mSubCoef > 0;
            subIncProgress(delta * mSubCoef);
        }
        public void subIncProgress(double realDelta) {
            mSubValue += realDelta;
            if (mParent != null) {
                mParent.subIncProgress(realDelta);
            } else {
                mRoot.internalIncProgress(realDelta);
            }
        }
        public boolean displayPrompt(String title, String message) {
            return mRoot.displayPrompt(title, message);
        }
        public ITaskMonitor createSubMonitor(int tickCount) {
            assert mSubCoef > 0;
            assert tickCount > 0;
            return new SubTaskMonitor(mRoot,
                    this,
                    mSubValue,
                    tickCount * mSubCoef);
        }
    }
}

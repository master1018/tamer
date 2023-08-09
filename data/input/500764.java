public abstract class Filter {
    private static final String LOG_TAG = "Filter";
    private static final String THREAD_NAME = "Filter";
    private static final int FILTER_TOKEN = 0xD0D0F00D;
    private static final int FINISH_TOKEN = 0xDEADBEEF;
    private Handler mThreadHandler;
    private Handler mResultHandler;
    private Delayer mDelayer;
    private final Object mLock = new Object();
    public Filter() {
        mResultHandler = new ResultsHandler();
    }
    public void setDelayer(Delayer delayer) {
        synchronized (mLock) {
            mDelayer = delayer;
        }
    }
    public final void filter(CharSequence constraint) {
        filter(constraint, null);
    }
    public final void filter(CharSequence constraint, FilterListener listener) {
        synchronized (mLock) {
            if (mThreadHandler == null) {
                HandlerThread thread = new HandlerThread(
                        THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                mThreadHandler = new RequestHandler(thread.getLooper());
            }
            final long delay = (mDelayer == null) ? 0 : mDelayer.getPostingDelay(constraint);
            Message message = mThreadHandler.obtainMessage(FILTER_TOKEN);
            RequestArguments args = new RequestArguments();
            args.constraint = constraint != null ? constraint.toString() : null;
            args.listener = listener;
            message.obj = args;
            mThreadHandler.removeMessages(FILTER_TOKEN);
            mThreadHandler.removeMessages(FINISH_TOKEN);
            mThreadHandler.sendMessageDelayed(message, delay);
        }
    }
    protected abstract FilterResults performFiltering(CharSequence constraint);
    protected abstract void publishResults(CharSequence constraint,
            FilterResults results);
    public CharSequence convertResultToString(Object resultValue) {
        return resultValue == null ? "" : resultValue.toString();
    }
    protected static class FilterResults {
        public FilterResults() {
        }
        public Object values;
        public int count;
    }
    public static interface FilterListener {
        public void onFilterComplete(int count);
    }
    private class RequestHandler extends Handler {
        public RequestHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            int what = msg.what;
            Message message;
            switch (what) {
                case FILTER_TOKEN:
                    RequestArguments args = (RequestArguments) msg.obj;
                    try {
                        args.results = performFiltering(args.constraint);
                    } catch (Exception e) {
                        args.results = new FilterResults();
                        Log.w(LOG_TAG, "An exception occured during performFiltering()!", e);
                    } finally {
                        message = mResultHandler.obtainMessage(what);
                        message.obj = args;
                        message.sendToTarget();
                    }
                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            Message finishMessage = mThreadHandler.obtainMessage(FINISH_TOKEN);
                            mThreadHandler.sendMessageDelayed(finishMessage, 3000);
                        }
                    }
                    break;
                case FINISH_TOKEN:
                    synchronized (mLock) {
                        if (mThreadHandler != null) {
                            mThreadHandler.getLooper().quit();
                            mThreadHandler = null;
                        }
                    }
                    break;
            }
        }
    }
    private class ResultsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            RequestArguments args = (RequestArguments) msg.obj;
            publishResults(args.constraint, args.results);
            if (args.listener != null) {
                int count = args.results != null ? args.results.count : -1;
                args.listener.onFilterComplete(count);
            }
        }
    }
    private static class RequestArguments {
        CharSequence constraint;
        FilterListener listener;
        FilterResults results;
    }
    public interface Delayer {
        long getPostingDelay(CharSequence constraint);
    }
}

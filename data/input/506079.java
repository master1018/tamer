public class EventLoader {
    private Context mContext;
    private Handler mHandler = new Handler();
    private AtomicInteger mSequenceNumber = new AtomicInteger();
    private LinkedBlockingQueue<LoadRequest> mLoaderQueue;
    private LoaderThread mLoaderThread;
    private ContentResolver mResolver;
    private static interface LoadRequest {
        public void processRequest(EventLoader eventLoader);
        public void skipRequest(EventLoader eventLoader);
    }
    private static class ShutdownRequest implements LoadRequest {
        public void processRequest(EventLoader eventLoader) {
        }
        public void skipRequest(EventLoader eventLoader) {
        }
    }
    private static class LoadEventDaysRequest implements LoadRequest {
        public int startDay;
        public int numDays;
        public boolean[] eventDays;
        public Runnable uiCallback;
        public LoadEventDaysRequest(int startDay, int numDays, boolean[] eventDays,
                final Runnable uiCallback)
        {
            this.startDay = startDay;
            this.numDays = numDays;
            this.eventDays = eventDays;
            this.uiCallback = uiCallback;
        }
        public void processRequest(EventLoader eventLoader)
        {
            final Handler handler = eventLoader.mHandler;
            ContentResolver cr = eventLoader.mResolver;
            Arrays.fill(eventDays, false);
            Cursor cursor = EventDays.query(cr, startDay, numDays);
            try {
                int startDayColumnIndex = cursor.getColumnIndexOrThrow(EventDays.STARTDAY);
                int endDayColumnIndex = cursor.getColumnIndexOrThrow(EventDays.ENDDAY);
                while (cursor.moveToNext()) {
                    int firstDay = cursor.getInt(startDayColumnIndex);
                    int lastDay = cursor.getInt(endDayColumnIndex);
                    int firstIndex = Math.max(firstDay - startDay, 0);
                    int lastIndex = Math.min(lastDay - startDay, 30);
                    for(int i = firstIndex; i <= lastIndex; i++) {
                        eventDays[i] = true;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            handler.post(uiCallback);
        }
        public void skipRequest(EventLoader eventLoader) {
        }
    }
    private static class LoadEventsRequest implements LoadRequest {
        public int id;
        public long startMillis;
        public int numDays;
        public ArrayList<Event> events;
        public Runnable successCallback;
        public Runnable cancelCallback;
        public LoadEventsRequest(int id, long startMillis, int numDays, ArrayList<Event> events,
                final Runnable successCallback, final Runnable cancelCallback) {
            this.id = id;
            this.startMillis = startMillis;
            this.numDays = numDays;
            this.events = events;
            this.successCallback = successCallback;
            this.cancelCallback = cancelCallback;
        }
        public void processRequest(EventLoader eventLoader) {
            Event.loadEvents(eventLoader.mContext, events, startMillis,
                    numDays, id, eventLoader.mSequenceNumber);
            if (id == eventLoader.mSequenceNumber.get()) {
                eventLoader.mHandler.post(successCallback);
            } else {
                eventLoader.mHandler.post(cancelCallback);
            }
        }
        public void skipRequest(EventLoader eventLoader) {
            eventLoader.mHandler.post(cancelCallback);
        }
    }
    private static class LoaderThread extends Thread {
        LinkedBlockingQueue<LoadRequest> mQueue;
        EventLoader mEventLoader;
        public LoaderThread(LinkedBlockingQueue<LoadRequest> queue, EventLoader eventLoader) {
            mQueue = queue;
            mEventLoader = eventLoader;
        }
        public void shutdown() {
            try {
                mQueue.put(new ShutdownRequest());
            } catch (InterruptedException ex) {
                Log.e("Cal", "LoaderThread.shutdown() interrupted!");
            }
        }
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                try {
                    LoadRequest request = mQueue.take();
                    while (!mQueue.isEmpty()) {
                        request.skipRequest(mEventLoader);
                        request = mQueue.take();
                    }
                    if (request instanceof ShutdownRequest) {
                        return;
                    }
                    request.processRequest(mEventLoader);
                } catch (InterruptedException ex) {
                    Log.e("Cal", "background LoaderThread interrupted!");
                }
            }
        }
    }
    public EventLoader(Context context) {
        mContext = context;
        mLoaderQueue = new LinkedBlockingQueue<LoadRequest>();
        mResolver = context.getContentResolver();
    }
    public void startBackgroundThread() {
        mLoaderThread = new LoaderThread(mLoaderQueue, this);
        mLoaderThread.start();
    }
    public void stopBackgroundThread() {
        mLoaderThread.shutdown();
    }
    void loadEventsInBackground(final int numDays, final ArrayList<Event> events,
            long start, final Runnable successCallback, final Runnable cancelCallback) {
        int id = mSequenceNumber.incrementAndGet();
        LoadEventsRequest request = new LoadEventsRequest(id, start, numDays,
                events, successCallback, cancelCallback);
        try {
            mLoaderQueue.put(request);
        } catch (InterruptedException ex) {
            Log.e("Cal", "loadEventsInBackground() interrupted!");
        }
    }
    void loadEventDaysInBackground(int startDay, int numDays, boolean[] eventDays,
        final Runnable uiCallback)
    {
        LoadEventDaysRequest request = new LoadEventDaysRequest(startDay, numDays,
                eventDays, uiCallback);
        try {
            mLoaderQueue.put(request);
        } catch (InterruptedException ex) {
            Log.e("Cal", "loadEventDaysInBackground() interrupted!");
        }
    }
}

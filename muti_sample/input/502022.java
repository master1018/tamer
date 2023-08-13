public class RequestQueue implements RequestFeeder {
    private final LinkedHashMap<HttpHost, LinkedList<Request>> mPending;
    private final Context mContext;
    private final ActivePool mActivePool;
    private final ConnectivityManager mConnectivityManager;
    private HttpHost mProxyHost = null;
    private BroadcastReceiver mProxyChangeReceiver;
    private static final int CONNECTION_COUNT = 4;
    class ActivePool implements ConnectionManager {
        ConnectionThread[] mThreads;
        IdleCache mIdleCache;
        private int mTotalRequest;
        private int mTotalConnection;
        private int mConnectionCount;
        ActivePool(int connectionCount) {
            mIdleCache = new IdleCache();
            mConnectionCount = connectionCount;
            mThreads = new ConnectionThread[mConnectionCount];
            for (int i = 0; i < mConnectionCount; i++) {
                mThreads[i] = new ConnectionThread(
                        mContext, i, this, RequestQueue.this);
            }
        }
        void startup() {
            for (int i = 0; i < mConnectionCount; i++) {
                mThreads[i].start();
            }
        }
        void shutdown() {
            for (int i = 0; i < mConnectionCount; i++) {
                mThreads[i].requestStop();
            }
        }
        void startConnectionThread() {
            synchronized (RequestQueue.this) {
                RequestQueue.this.notify();
            }
        }
        public void startTiming() {
            for (int i = 0; i < mConnectionCount; i++) {
                ConnectionThread rt = mThreads[i];
                rt.mCurrentThreadTime = -1;
                rt.mTotalThreadTime = 0;
            }
            mTotalRequest = 0;
            mTotalConnection = 0;
        }
        public void stopTiming() {
            int totalTime = 0;
            for (int i = 0; i < mConnectionCount; i++) {
                ConnectionThread rt = mThreads[i];
                if (rt.mCurrentThreadTime != -1) {
                    totalTime += rt.mTotalThreadTime;
                }
                rt.mCurrentThreadTime = 0;
            }
            Log.d("Http", "Http thread used " + totalTime + " ms " + " for "
                    + mTotalRequest + " requests and " + mTotalConnection
                    + " new connections");
        }
        void logState() {
            StringBuilder dump = new StringBuilder();
            for (int i = 0; i < mConnectionCount; i++) {
                dump.append(mThreads[i] + "\n");
            }
            HttpLog.v(dump.toString());
        }
        public HttpHost getProxyHost() {
            return mProxyHost;
        }
        void disablePersistence() {
            for (int i = 0; i < mConnectionCount; i++) {
                Connection connection = mThreads[i].mConnection;
                if (connection != null) connection.setCanPersist(false);
            }
            mIdleCache.clear();
        }
        ConnectionThread getThread(HttpHost host) {
            synchronized(RequestQueue.this) {
                for (int i = 0; i < mThreads.length; i++) {
                    ConnectionThread ct = mThreads[i];
                    Connection connection = ct.mConnection;
                    if (connection != null && connection.mHost.equals(host)) {
                        return ct;
                    }
                }
            }
            return null;
        }
        public Connection getConnection(Context context, HttpHost host) {
            host = RequestQueue.this.determineHost(host);
            Connection con = mIdleCache.getConnection(host);
            if (con == null) {
                mTotalConnection++;
                con = Connection.getConnection(mContext, host, mProxyHost,
                        RequestQueue.this);
            }
            return con;
        }
        public boolean recycleConnection(Connection connection) {
            return mIdleCache.cacheConnection(connection.getHost(), connection);
        }
    }
    public RequestQueue(Context context) {
        this(context, CONNECTION_COUNT);
    }
    public RequestQueue(Context context, int connectionCount) {
        mContext = context;
        mPending = new LinkedHashMap<HttpHost, LinkedList<Request>>(32);
        mActivePool = new ActivePool(connectionCount);
        mActivePool.startup();
        mConnectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public synchronized void enablePlatformNotifications() {
        if (HttpLog.LOGV) HttpLog.v("RequestQueue.enablePlatformNotifications() network");
        if (mProxyChangeReceiver == null) {
            mProxyChangeReceiver =
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context ctx, Intent intent) {
                            setProxyConfig();
                        }
                    };
            mContext.registerReceiver(mProxyChangeReceiver,
                                      new IntentFilter(Proxy.PROXY_CHANGE_ACTION));
        }
        setProxyConfig();
    }
    public synchronized void disablePlatformNotifications() {
        if (HttpLog.LOGV) HttpLog.v("RequestQueue.disablePlatformNotifications() network");
        if (mProxyChangeReceiver != null) {
            mContext.unregisterReceiver(mProxyChangeReceiver);
            mProxyChangeReceiver = null;
        }
    }
    private synchronized void setProxyConfig() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            mProxyHost = null;
        } else {
            String host = Proxy.getHost(mContext);
            if (HttpLog.LOGV) HttpLog.v("RequestQueue.setProxyConfig " + host);
            if (host == null) {
                mProxyHost = null;
            } else {
                mActivePool.disablePersistence();
                mProxyHost = new HttpHost(host, Proxy.getPort(mContext), "http");
            }
        }
    }
    public HttpHost getProxyHost() {
        return mProxyHost;
    }
    public RequestHandle queueRequest(
            String url, String method,
            Map<String, String> headers, EventHandler eventHandler,
            InputStream bodyProvider, int bodyLength) {
        WebAddress uri = new WebAddress(url);
        return queueRequest(url, uri, method, headers, eventHandler,
                            bodyProvider, bodyLength);
    }
    public RequestHandle queueRequest(
            String url, WebAddress uri, String method, Map<String, String> headers,
            EventHandler eventHandler,
            InputStream bodyProvider, int bodyLength) {
        if (HttpLog.LOGV) HttpLog.v("RequestQueue.queueRequest " + uri);
        if (eventHandler == null) {
            eventHandler = new LoggingEventHandler();
        }
        Request req;
        HttpHost httpHost = new HttpHost(uri.mHost, uri.mPort, uri.mScheme);
        req = new Request(method, httpHost, mProxyHost, uri.mPath, bodyProvider,
                          bodyLength, eventHandler, headers);
        queueRequest(req, false);
        mActivePool.mTotalRequest++;
        mActivePool.startConnectionThread();
        return new RequestHandle(
                this, url, uri, method, headers, bodyProvider, bodyLength,
                req);
    }
    private static class SyncFeeder implements RequestFeeder {
        private Request mRequest;
        SyncFeeder() {
        }
        public Request getRequest() {
            Request r = mRequest;
            mRequest = null;
            return r;
        }
        public Request getRequest(HttpHost host) {
            return getRequest();
        }
        public boolean haveRequest(HttpHost host) {
            return mRequest != null;
        }
        public void requeueRequest(Request r) {
            mRequest = r;
        }
    }
    public RequestHandle queueSynchronousRequest(String url, WebAddress uri,
            String method, Map<String, String> headers,
            EventHandler eventHandler, InputStream bodyProvider,
            int bodyLength) {
        if (HttpLog.LOGV) {
            HttpLog.v("RequestQueue.dispatchSynchronousRequest " + uri);
        }
        HttpHost host = new HttpHost(uri.mHost, uri.mPort, uri.mScheme);
        Request req = new Request(method, host, mProxyHost, uri.mPath,
                bodyProvider, bodyLength, eventHandler, headers);
        host = determineHost(host);
        Connection conn = Connection.getConnection(mContext, host, mProxyHost,
                new SyncFeeder());
        return new RequestHandle(this, url, uri, method, headers, bodyProvider,
                bodyLength, req, conn);
    }
    private HttpHost determineHost(HttpHost host) {
        return (mProxyHost == null || "https".equals(host.getSchemeName()))
                ? host : mProxyHost;
    }
    synchronized boolean requestsPending() {
        return !mPending.isEmpty();
    }
    synchronized void dump() {
        HttpLog.v("dump()");
        StringBuilder dump = new StringBuilder();
        int count = 0;
        Iterator<Map.Entry<HttpHost, LinkedList<Request>>> iter;
        if (!mPending.isEmpty()) {
            iter = mPending.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<HttpHost, LinkedList<Request>> entry = iter.next();
                String hostName = entry.getKey().getHostName();
                StringBuilder line = new StringBuilder("p" + count++ + " " + hostName + " ");
                LinkedList<Request> reqList = entry.getValue();
                ListIterator reqIter = reqList.listIterator(0);
                while (iter.hasNext()) {
                    Request request = (Request)iter.next();
                    line.append(request + " ");
                }
                dump.append(line);
                dump.append("\n");
            }
        }
        HttpLog.v(dump.toString());
    }
    public synchronized Request getRequest() {
        Request ret = null;
        if (!mPending.isEmpty()) {
            ret = removeFirst(mPending);
        }
        if (HttpLog.LOGV) HttpLog.v("RequestQueue.getRequest() => " + ret);
        return ret;
    }
    public synchronized Request getRequest(HttpHost host) {
        Request ret = null;
        if (mPending.containsKey(host)) {
            LinkedList<Request> reqList = mPending.get(host);
            ret = reqList.removeFirst();
            if (reqList.isEmpty()) {
                mPending.remove(host);
            }
        }
        if (HttpLog.LOGV) HttpLog.v("RequestQueue.getRequest(" + host + ") => " + ret);
        return ret;
    }
    public synchronized boolean haveRequest(HttpHost host) {
        return mPending.containsKey(host);
    }
    public void requeueRequest(Request request) {
        queueRequest(request, true);
    }
    public void shutdown() {
        mActivePool.shutdown();
    }
    protected synchronized void queueRequest(Request request, boolean head) {
        HttpHost host = request.mProxyHost == null ? request.mHost : request.mProxyHost;
        LinkedList<Request> reqList;
        if (mPending.containsKey(host)) {
            reqList = mPending.get(host);
        } else {
            reqList = new LinkedList<Request>();
            mPending.put(host, reqList);
        }
        if (head) {
            reqList.addFirst(request);
        } else {
            reqList.add(request);
        }
    }
    public void startTiming() {
        mActivePool.startTiming();
    }
    public void stopTiming() {
        mActivePool.stopTiming();
    }
    private Request removeFirst(LinkedHashMap<HttpHost, LinkedList<Request>> requestQueue) {
        Request ret = null;
        Iterator<Map.Entry<HttpHost, LinkedList<Request>>> iter = requestQueue.entrySet().iterator();
        if (iter.hasNext()) {
            Map.Entry<HttpHost, LinkedList<Request>> entry = iter.next();
            LinkedList<Request> reqList = entry.getValue();
            ret = reqList.removeFirst();
            if (reqList.isEmpty()) {
                requestQueue.remove(entry.getKey());
            }
        }
        return ret;
    }
    interface ConnectionManager {
        HttpHost getProxyHost();
        Connection getConnection(Context context, HttpHost host);
        boolean recycleConnection(Connection connection);
    }
}

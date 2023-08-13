class HttpCirChannel extends CirChannel implements Runnable {
    private static final int HTTP_CIR_PING_INTERVAL = 10000;
    private DataChannel mDataChannel;
    private boolean mStopped;
    private Thread mPollingTask;
    private URL mCirUrl;
    private long mServerPollMin;
    public HttpCirChannel(ImpsConnection connection, DataChannel dataChannel) {
        super(connection);
        this.mDataChannel = dataChannel;
    }
    @Override
    public synchronized void connect() {
        ImpsSession session = mConnection.getSession();
        try {
            if (session.getCirHttpAddress() != null) {
                mCirUrl = new URL(session.getCirHttpAddress());
            }
        } catch (MalformedURLException e) {
        }
        mServerPollMin = session.getServerPollMin() * 1000;
        mStopped = false;
        mPollingTask = new Thread(this, "HTTPCIRChannel");
        mPollingTask.setDaemon(true);
        mPollingTask.start();
    }
    public synchronized boolean isShutdown() {
        return mStopped;
    }
    @Override
    public synchronized void shutdown() {
        mStopped = true;
    }
    public void run() {
        while (!mStopped) {
            long lastActive = mDataChannel.getLastActiveTime();
            if (mCirUrl != null) {
                if (SystemClock.elapsedRealtime() - lastActive >= HTTP_CIR_PING_INTERVAL) {
                    HttpURLConnection urlConnection;
                    try {
                        urlConnection = (HttpURLConnection) mCirUrl
                                .openConnection();
                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            mConnection.sendPollingRequest();
                        }
                    } catch (IOException e) {
                        mStopped = true;
                    }
                }
                try {
                    Thread.sleep(HTTP_CIR_PING_INTERVAL);
                } catch (InterruptedException e) {
                }
            } else {
                if (SystemClock.elapsedRealtime() - lastActive >= mServerPollMin
                        && mDataChannel.isSendingQueueEmpty()) {
                    mConnection.sendPollingRequest();
                }
                try {
                    Thread.sleep(mServerPollMin);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

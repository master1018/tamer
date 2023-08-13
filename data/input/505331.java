public class HttpAuthHandler extends Handler {
    private static final String LOGTAG = "network";
    private Network mNetwork;
    private LinkedList<LoadListener> mLoaderQueue;
    private static final int AUTH_PROCEED = 100;
    private static final int AUTH_CANCEL = 200;
    Object mRequestInFlightLock = new Object();
    boolean mRequestInFlight;
    String mUsername;
    String mPassword;
     HttpAuthHandler(Network network) {
        mNetwork = network;
        mLoaderQueue = new LinkedList<LoadListener>();
    }
    @Override
    public void handleMessage(Message msg) {
        LoadListener loader = null;
        synchronized (mLoaderQueue) {
            loader = mLoaderQueue.poll();
        }
        assert(loader.isSynchronous() == false);
        switch (msg.what) {
            case AUTH_PROCEED:
                String username = msg.getData().getString("username");
                String password = msg.getData().getString("password");
                loader.handleAuthResponse(username, password);
                break;
            case AUTH_CANCEL:
                loader.handleAuthResponse(null, null);
                break;
        }
        processNextLoader();
    }
    private boolean handleResponseForSynchronousRequest(String username, String password) {
        LoadListener loader = null;
        synchronized (mLoaderQueue) {
            loader = mLoaderQueue.peek();
        }
        if (loader.isSynchronous()) {
            mUsername = username;
            mPassword = password;
            return true;
        }
        return false;
    }
    private void signalRequestComplete() {
        synchronized (mRequestInFlightLock) {
            assert(mRequestInFlight);
            mRequestInFlight = false;
            mRequestInFlightLock.notify();
        }
    }
    public void proceed(String username, String password) {
        if (handleResponseForSynchronousRequest(username, password)) {
            signalRequestComplete();
            return;
        }
        Message msg = obtainMessage(AUTH_PROCEED);
        msg.getData().putString("username", username);
        msg.getData().putString("password", password);
        sendMessage(msg);
        signalRequestComplete();
    }
    public void cancel() {
        if (handleResponseForSynchronousRequest(null, null)) {
            signalRequestComplete();
            return;
        }
        sendMessage(obtainMessage(AUTH_CANCEL));
        signalRequestComplete();
    }
    public boolean useHttpAuthUsernamePassword() {
        LoadListener loader = null;
        synchronized (mLoaderQueue) {
            loader = mLoaderQueue.peek();
        }
        if (loader != null) {
            return !loader.authCredentialsInvalid();
        }
        return false;
    }
     void handleAuthRequest(LoadListener loader) {
        if (loader.isSynchronous()) {
            waitForRequestToComplete();
            synchronized (mLoaderQueue) {
                mLoaderQueue.addFirst(loader);
            }
            processNextLoader();
            waitForRequestToComplete();
            synchronized (mLoaderQueue) {
                assert(mLoaderQueue.peek() == loader);
                mLoaderQueue.poll();
            }
            loader.handleAuthResponse(mUsername, mPassword);
            return;
        }
        boolean processNext = false;
        synchronized (mLoaderQueue) {
            mLoaderQueue.offer(loader);
            processNext =
                (mLoaderQueue.size() == 1);
        }
        if (processNext) {
            processNextLoader();
        }
    }
    private void waitForRequestToComplete() {
        synchronized (mRequestInFlightLock) {
            while (mRequestInFlight) {
                try {
                    mRequestInFlightLock.wait();
                } catch(InterruptedException e) {
                    Log.e(LOGTAG, "Interrupted while waiting for request to complete");
                }
            }
        }
    }
    private void processNextLoader() {
        LoadListener loader = null;
        synchronized (mLoaderQueue) {
            loader = mLoaderQueue.peek();
        }
        if (loader != null) {
            synchronized (mRequestInFlightLock) {
                assert(mRequestInFlight == false);
                mRequestInFlight = true;
            }
            CallbackProxy proxy = loader.getFrame().getCallbackProxy();
            String hostname = loader.proxyAuthenticate() ?
                mNetwork.getProxyHostname() : loader.host();
            String realm = loader.realm();
            proxy.onReceivedHttpAuthRequest(this, hostname, realm);
        }
    }
    public static void onReceivedCredentials(LoadListener loader,
            String host, String realm, String username, String password) {
        CallbackProxy proxy = loader.getFrame().getCallbackProxy();
        proxy.onReceivedHttpAuthCredentials(host, realm, username, password);
    }
}

public class BluetoothOppRfcommListener {
    private static final String TAG = "BtOppRfcommListener";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    public static final int MSG_INCOMING_BTOPP_CONNECTION = 100;
    private volatile boolean mInterrupted;
    private Thread mSocketAcceptThread;
    private Handler mCallback;
    private static final int CREATE_RETRY_TIME = 10;
    private static final int DEFAULT_OPP_CHANNEL = 12;
    private final int mBtOppRfcommChannel;
    private final BluetoothAdapter mAdapter;
    private BluetoothServerSocket mBtServerSocket = null;
    private ServerSocket mTcpServerSocket = null;
    public BluetoothOppRfcommListener(BluetoothAdapter adapter) {
        this(adapter, DEFAULT_OPP_CHANNEL);
    }
    public BluetoothOppRfcommListener(BluetoothAdapter adapter, int channel) {
        mBtOppRfcommChannel = channel;
        mAdapter = adapter;
    }
    public synchronized boolean start(Handler callback) {
        if (mSocketAcceptThread == null) {
            mCallback = callback;
            mSocketAcceptThread = new Thread(TAG) {
                public void run() {
                    if (Constants.USE_TCP_DEBUG) {
                        try {
                            if (V) Log.v(TAG, "Create TCP ServerSocket");
                            mTcpServerSocket = new ServerSocket(Constants.TCP_DEBUG_PORT, 1);
                        } catch (IOException e) {
                            Log.e(TAG, "Error listing on port" + Constants.TCP_DEBUG_PORT);
                            mInterrupted = true;
                        }
                        while (!mInterrupted) {
                            try {
                                Socket clientSocket = mTcpServerSocket.accept();
                                if (V) Log.v(TAG, "Socket connected!");
                                TestTcpTransport transport = new TestTcpTransport(clientSocket);
                                Message msg = Message.obtain();
                                msg.setTarget(mCallback);
                                msg.what = MSG_INCOMING_BTOPP_CONNECTION;
                                msg.obj = transport;
                                msg.sendToTarget();
                            } catch (IOException e) {
                                Log.e(TAG, "Error accept connection " + e);
                            }
                        }
                        if (V) Log.v(TAG, "TCP listen thread finished");
                    } else {
                        boolean serverOK = true;
                        for (int i = 0; i < CREATE_RETRY_TIME && !mInterrupted; i++) {
                            try {
                                mBtServerSocket = mAdapter
                                        .listenUsingInsecureRfcommOn(mBtOppRfcommChannel);
                            } catch (IOException e1) {
                                Log.e(TAG, "Error create RfcommServerSocket " + e1);
                                serverOK = false;
                            }
                            if (!serverOK) {
                                synchronized (this) {
                                    try {
                                        if (V) Log.v(TAG, "wait 3 seconds");
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        Log.e(TAG, "socketAcceptThread thread was interrupted (3)");
                                        mInterrupted = true;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        if (!serverOK) {
                            Log.e(TAG, "Error start listening after " + CREATE_RETRY_TIME + " try");
                            mInterrupted = true;
                        }
                        if (!mInterrupted) {
                            Log.i(TAG, "Accept thread started on channel " + mBtOppRfcommChannel);
                        }
                        BluetoothSocket clientSocket;
                        while (!mInterrupted) {
                            try {
                                clientSocket = mBtServerSocket.accept();
                                Log.i(TAG, "Accepted connectoin from "
                                        + clientSocket.getRemoteDevice());
                                BluetoothOppRfcommTransport transport = new BluetoothOppRfcommTransport(
                                        clientSocket);
                                Message msg = Message.obtain();
                                msg.setTarget(mCallback);
                                msg.what = MSG_INCOMING_BTOPP_CONNECTION;
                                msg.obj = transport;
                                msg.sendToTarget();
                            } catch (IOException e) {
                                Log.e(TAG, "Error accept connection " + e);
                            }
                        }
                        Log.i(TAG, "BluetoothSocket listen thread finished");
                    }
                }
            };
            mInterrupted = false;
            if(!Constants.USE_TCP_SIMPLE_SERVER) {
                mSocketAcceptThread.start();
            }
        }
        return true;
    }
    public synchronized void stop() {
        if (mSocketAcceptThread != null) {
            Log.i(TAG, "stopping Accept Thread");
            mInterrupted = true;
            if (Constants.USE_TCP_DEBUG) {
                if (V) Log.v(TAG, "close mTcpServerSocket");
                if (mTcpServerSocket != null) {
                    try {
                        mTcpServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error close mTcpServerSocket");
                    }
                }
            } else {
                if (V) Log.v(TAG, "close mBtServerSocket");
                if (mBtServerSocket != null) {
                    try {
                        mBtServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error close mBtServerSocket");
                    }
                }
            }
            try {
                mSocketAcceptThread.interrupt();
                if (V) Log.v(TAG, "waiting for thread to terminate");
                mSocketAcceptThread.join();
                mSocketAcceptThread = null;
                mCallback = null;
            } catch (InterruptedException e) {
                if (V) Log.v(TAG, "Interrupted waiting for Accept Thread to join");
            }
        }
    }
}

public final class BluetoothServerSocket implements Closeable {
     final BluetoothSocket mSocket;
    private Handler mHandler;
    private int mMessage;
     BluetoothServerSocket(int type, boolean auth, boolean encrypt, int port)
            throws IOException {
        mSocket = new BluetoothSocket(type, -1, auth, encrypt, null, port, null);
    }
    public BluetoothSocket accept() throws IOException {
        return accept(-1);
    }
    public BluetoothSocket accept(int timeout) throws IOException {
        return mSocket.accept(timeout);
    }
    public void close() throws IOException {
        synchronized (this) {
            if (mHandler != null) {
                mHandler.obtainMessage(mMessage).sendToTarget();
            }
        }
        mSocket.close();
    }
     synchronized void setCloseHandler(Handler handler, int message) {
        mHandler = handler;
        mMessage = message;
    }
}

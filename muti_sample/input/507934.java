public final class BluetoothSocket implements Closeable {
    private static final String TAG = "BluetoothSocket";
    public static final int MAX_RFCOMM_CHANNEL = 30;
     static final int TYPE_RFCOMM = 1;
     static final int TYPE_SCO = 2;
     static final int TYPE_L2CAP = 3;
     static final int EBADFD = 77;
     static final int EADDRINUSE = 98;
    private final int mType;  
    private final BluetoothDevice mDevice;    
    private final String mAddress;    
    private final boolean mAuth;
    private final boolean mEncrypt;
    private final BluetoothInputStream mInputStream;
    private final BluetoothOutputStream mOutputStream;
    private final SdpHelper mSdp;
    private int mPort;  
    private boolean mClosed;
    private final ReentrantReadWriteLock mLock;
    private int mSocketData;
     BluetoothSocket(int type, int fd, boolean auth, boolean encrypt,
            BluetoothDevice device, int port, ParcelUuid uuid) throws IOException {
        if (type == BluetoothSocket.TYPE_RFCOMM && uuid == null && fd == -1) {
            if (port < 1 || port > MAX_RFCOMM_CHANNEL) {
                throw new IOException("Invalid RFCOMM channel: " + port);
            }
        }
        if (uuid == null) {
            mPort = port;
            mSdp = null;
        } else {
            mSdp = new SdpHelper(device, uuid);
            mPort = -1;
        }
        mType = type;
        mAuth = auth;
        mEncrypt = encrypt;
        mDevice = device;
        if (device == null) {
            mAddress = null;
        } else {
            mAddress = device.getAddress();
        }
        if (fd == -1) {
            initSocketNative();
        } else {
            initSocketFromFdNative(fd);
        }
        mInputStream = new BluetoothInputStream(this);
        mOutputStream = new BluetoothOutputStream(this);
        mClosed = false;
        mLock = new ReentrantReadWriteLock();
    }
    private BluetoothSocket(int type, int fd, boolean auth, boolean encrypt, String address,
            int port) throws IOException {
        this(type, fd, auth, encrypt, new BluetoothDevice(address), port, null);
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
    public void connect() throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) throw new IOException("socket closed");
            if (mSdp != null) {
                mPort = mSdp.doSdp();  
            }
            connectNative();  
        } finally {
            mLock.readLock().unlock();
        }
    }
    public void close() throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) return;
            if (mSdp != null) {
                mSdp.cancel();
            }
            abortNative();
        } finally {
            mLock.readLock().unlock();
        }
        mLock.writeLock().lock();
        try {
            mClosed = true;
            destroyNative();
        } finally {
            mLock.writeLock().unlock();
        }
    }
    public BluetoothDevice getRemoteDevice() {
        return mDevice;
    }
    public InputStream getInputStream() throws IOException {
        return mInputStream;
    }
    public OutputStream getOutputStream() throws IOException {
        return mOutputStream;
    }
     int bindListen() {
        mLock.readLock().lock();
        try {
            if (mClosed) return EBADFD;
            return bindListenNative();
        } finally {
            mLock.readLock().unlock();
        }
    }
     BluetoothSocket accept(int timeout) throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) throw new IOException("socket closed");
            return acceptNative(timeout);
        } finally {
            mLock.readLock().unlock();
        }
    }
     int available() throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) throw new IOException("socket closed");
            return availableNative();
        } finally {
            mLock.readLock().unlock();
        }
    }
     int read(byte[] b, int offset, int length) throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) throw new IOException("socket closed");
            return readNative(b, offset, length);
        } finally {
            mLock.readLock().unlock();
        }
    }
     int write(byte[] b, int offset, int length) throws IOException {
        mLock.readLock().lock();
        try {
            if (mClosed) throw new IOException("socket closed");
            return writeNative(b, offset, length);
        } finally {
            mLock.readLock().unlock();
        }
    }
    private native void initSocketNative() throws IOException;
    private native void initSocketFromFdNative(int fd) throws IOException;
    private native void connectNative() throws IOException;
    private native int bindListenNative();
    private native BluetoothSocket acceptNative(int timeout) throws IOException;
    private native int availableNative() throws IOException;
    private native int readNative(byte[] b, int offset, int length) throws IOException;
    private native int writeNative(byte[] b, int offset, int length) throws IOException;
    private native void abortNative() throws IOException;
    private native void destroyNative() throws IOException;
     native void throwErrnoNative(int errno) throws IOException;
    private static class SdpHelper extends IBluetoothCallback.Stub {
        private final IBluetooth service;
        private final ParcelUuid uuid;
        private final BluetoothDevice device;
        private int channel;
        private boolean canceled;
        public SdpHelper(BluetoothDevice device, ParcelUuid uuid) {
            service = BluetoothDevice.getService();
            this.device = device;
            this.uuid = uuid;
            canceled = false;
        }
        public synchronized int doSdp() throws IOException {
            if (canceled) throw new IOException("Service discovery canceled");
            channel = -1;
            boolean inProgress = false;
            try {
                inProgress = service.fetchRemoteUuids(device.getAddress(), uuid, this);
            } catch (RemoteException e) {Log.e(TAG, "", e);}
            if (!inProgress) throw new IOException("Unable to start Service Discovery");
            try {
                wait(12000);   
            } catch (InterruptedException e) {}
            if (canceled) throw new IOException("Service discovery canceled");
            if (channel < 1) throw new IOException("Service discovery failed");
            return channel;
        }
        public synchronized void cancel() {
            if (!canceled) {
                canceled = true;
                channel = -1;
                notifyAll();  
            }
        }
        public synchronized void onRfcommChannelFound(int channel) {
            if (!canceled) {
                this.channel = channel;
                notifyAll();  
            }
        }
    }
}

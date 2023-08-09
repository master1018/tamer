class Debugger {
    private static final int INITIAL_BUF_SIZE = 1 * 1024;
    private static final int MAX_BUF_SIZE = 32 * 1024;
    private ByteBuffer mReadBuffer;
    private static final int PRE_DATA_BUF_SIZE = 256;
    private ByteBuffer mPreDataBuffer;
    private int mConnState;
    private static final int ST_NOT_CONNECTED = 1;
    private static final int ST_AWAIT_SHAKE   = 2;
    private static final int ST_READY         = 3;
    private Client mClient;         
    private int mListenPort;        
    private ServerSocketChannel mListenChannel;
    private SocketChannel mChannel;
    Debugger(Client client, int listenPort) throws IOException {
        mClient = client;
        mListenPort = listenPort;
        mListenChannel = ServerSocketChannel.open();
        mListenChannel.configureBlocking(false);        
        InetSocketAddress addr = new InetSocketAddress(
                InetAddress.getByName("localhost"), 
                listenPort);
        mListenChannel.socket().setReuseAddress(true);  
        mListenChannel.socket().bind(addr);
        mReadBuffer = ByteBuffer.allocate(INITIAL_BUF_SIZE);
        mPreDataBuffer = ByteBuffer.allocate(PRE_DATA_BUF_SIZE);
        mConnState = ST_NOT_CONNECTED;
        Log.d("ddms", "Created: " + this.toString());
    }
    boolean isDebuggerAttached() {
        return mChannel != null;
    }
    @Override
    public String toString() {
        return "[Debugger " + mListenPort + "-->" + mClient.getClientData().getPid()
                + ((mConnState != ST_READY) ? " inactive]" : " active]");
    }
    void registerListener(Selector sel) throws IOException {
        mListenChannel.register(sel, SelectionKey.OP_ACCEPT, this);
    }
    Client getClient() {
        return mClient;
    }
    synchronized SocketChannel accept() throws IOException {
        return accept(mListenChannel);
    }
    synchronized SocketChannel accept(ServerSocketChannel listenChan)
        throws IOException {
        if (listenChan != null) {
            SocketChannel newChan;
            newChan = listenChan.accept();
            if (mChannel != null) {
                Log.w("ddms", "debugger already talking to " + mClient
                    + " on " + mListenPort);
                newChan.close();
                return null;
            }
            mChannel = newChan;
            mChannel.configureBlocking(false);         
            mConnState = ST_AWAIT_SHAKE;
            return mChannel;
        }
        return null;
    }
    synchronized void closeData() {
        try {
            if (mChannel != null) {
                mChannel.close();
                mChannel = null;
                mConnState = ST_NOT_CONNECTED;
                ClientData cd = mClient.getClientData();
                cd.setDebuggerConnectionStatus(DebuggerStatus.DEFAULT);
                mClient.update(Client.CHANGE_DEBUGGER_STATUS);
            }
        } catch (IOException ioe) {
            Log.w("ddms", "Failed to close data " + this);
        }
    }
    synchronized void close() {
        try {
            if (mListenChannel != null) {
                mListenChannel.close();
            }
            mListenChannel = null;
            closeData();
        } catch (IOException ioe) {
            Log.w("ddms", "Failed to close listener " + this);
        }
    }
    void read() throws IOException {
        int count;
        if (mReadBuffer.position() == mReadBuffer.capacity()) {
            if (mReadBuffer.capacity() * 2 > MAX_BUF_SIZE) {
                throw new BufferOverflowException();
            }
            Log.d("ddms", "Expanding read buffer to "
                + mReadBuffer.capacity() * 2);
            ByteBuffer newBuffer =
                    ByteBuffer.allocate(mReadBuffer.capacity() * 2);
            mReadBuffer.position(0);
            newBuffer.put(mReadBuffer);     
            mReadBuffer = newBuffer;
        }
        count = mChannel.read(mReadBuffer);
        Log.v("ddms", "Read " + count + " bytes from " + this);
        if (count < 0) throw new IOException("read failed");
    }
    JdwpPacket getJdwpPacket() throws IOException {
        if (mConnState == ST_AWAIT_SHAKE) {
            int result;
            result = JdwpPacket.findHandshake(mReadBuffer);
            switch (result) {
                case JdwpPacket.HANDSHAKE_GOOD:
                    Log.d("ddms", "Good handshake from debugger");
                    JdwpPacket.consumeHandshake(mReadBuffer);
                    sendHandshake();
                    mConnState = ST_READY;
                    ClientData cd = mClient.getClientData();
                    cd.setDebuggerConnectionStatus(DebuggerStatus.ATTACHED);
                    mClient.update(Client.CHANGE_DEBUGGER_STATUS);
                    return getJdwpPacket();
                case JdwpPacket.HANDSHAKE_BAD:
                    Log.d("ddms", "Bad handshake from debugger");
                    throw new IOException("bad handshake");
                case JdwpPacket.HANDSHAKE_NOTYET:
                    break;
                default:
                    Log.e("ddms", "Unknown packet while waiting for client handshake");
            }
            return null;
        } else if (mConnState == ST_READY) {
            if (mReadBuffer.position() != 0) {
                Log.v("ddms", "Checking " + mReadBuffer.position() + " bytes");
            }
            return JdwpPacket.findPacket(mReadBuffer);
        } else {
            Log.e("ddms", "Receiving data in state = " + mConnState);
        }
        return null;
    }
    void forwardPacketToClient(JdwpPacket packet) throws IOException {
        mClient.sendAndConsume(packet);
    }
    private synchronized void sendHandshake() throws IOException {
        ByteBuffer tempBuffer = ByteBuffer.allocate(JdwpPacket.HANDSHAKE_LEN);
        JdwpPacket.putHandshake(tempBuffer);
        int expectedLength = tempBuffer.position();
        tempBuffer.flip();
        if (mChannel.write(tempBuffer) != expectedLength) {
            throw new IOException("partial handshake write");
        }
        expectedLength = mPreDataBuffer.position();
        if (expectedLength > 0) {
            Log.d("ddms", "Sending " + mPreDataBuffer.position()
                    + " bytes of saved data");
            mPreDataBuffer.flip();
            if (mChannel.write(mPreDataBuffer) != expectedLength) {
                throw new IOException("partial pre-data write");
            }
            mPreDataBuffer.clear();
        }
    }
    synchronized void sendAndConsume(JdwpPacket packet)
        throws IOException {
        if (mChannel == null) {
            Log.d("ddms", "Saving packet 0x"
                    + Integer.toHexString(packet.getId()));
            packet.movePacket(mPreDataBuffer);
        } else {
            packet.writeAndConsume(mChannel);
        }
    }
}

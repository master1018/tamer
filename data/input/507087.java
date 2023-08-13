final class MonitorThread extends Thread {
    private static final int CLIENT_READY = 2;
    private static final int CLIENT_DISCONNECTED = 3;
    private volatile boolean mQuit = false;
    private ArrayList<Client> mClientList;
    private Selector mSelector;
    private HashMap<Integer, ChunkHandler> mHandlerMap;
    private ServerSocketChannel mDebugSelectedChan;
    private int mNewDebugSelectedPort;
    private int mDebugSelectedPort = -1;
    private Client mSelectedClient = null;
    private static MonitorThread mInstance;
    private MonitorThread() {
        super("Monitor");
        mClientList = new ArrayList<Client>();
        mHandlerMap = new HashMap<Integer, ChunkHandler>();
        mNewDebugSelectedPort = DdmPreferences.getSelectedDebugPort();
    }
    static MonitorThread createInstance() {
        return mInstance = new MonitorThread();
    }
    static MonitorThread getInstance() {
        return mInstance;
    }
    synchronized void setDebugSelectedPort(int port) throws IllegalStateException {
        if (mInstance == null) {
            return;
        }
        if (AndroidDebugBridge.getClientSupport() == false) {
            return;
        }
        if (mDebugSelectedChan != null) {
            Log.d("ddms", "Changing debug-selected port to " + port);
            mNewDebugSelectedPort = port;
            wakeup();
        } else {
            mNewDebugSelectedPort = port;
        }
    }
    synchronized void setSelectedClient(Client selectedClient) {
        if (mInstance == null) {
            return;
        }
        if (mSelectedClient != selectedClient) {
            Client oldClient = mSelectedClient;
            mSelectedClient = selectedClient;
            if (oldClient != null) {
                oldClient.update(Client.CHANGE_PORT);
            }
            if (mSelectedClient != null) {
                mSelectedClient.update(Client.CHANGE_PORT);
            }
        }
    }
    Client getSelectedClient() {
        return mSelectedClient;
    }
    boolean getRetryOnBadHandshake() {
        return true; 
    }
    Client[] getClients() {
        synchronized (mClientList) {
            return mClientList.toArray(new Client[0]);
        }
    }
    synchronized void registerChunkHandler(int type, ChunkHandler handler) {
        if (mInstance == null) {
            return;
        }
        synchronized (mHandlerMap) {
            if (mHandlerMap.get(type) == null) {
                mHandlerMap.put(type, handler);
            }
        }
    }
    @Override
    public void run() {
        Log.d("ddms", "Monitor is up");
        try {
            mSelector = Selector.open();
        } catch (IOException ioe) {
            Log.logAndDisplay(LogLevel.ERROR, "ddms",
                    "Failed to initialize Monitor Thread: " + ioe.getMessage());
            return;
        }
        while (!mQuit) {
            try {
                synchronized (mClientList) {
                }
                try {
                    if (AndroidDebugBridge.getClientSupport()) {
                        if ((mDebugSelectedChan == null ||
                                mNewDebugSelectedPort != mDebugSelectedPort) &&
                                mNewDebugSelectedPort != -1) {
                            if (reopenDebugSelectedPort()) {
                                mDebugSelectedPort = mNewDebugSelectedPort;
                            }
                        }
                    }
                } catch (IOException ioe) {
                    Log.e("ddms",
                            "Failed to reopen debug port for Selected Client to: " + mNewDebugSelectedPort);
                    Log.e("ddms", ioe);
                    mNewDebugSelectedPort = mDebugSelectedPort; 
                }
                int count;
                try {
                    count = mSelector.select();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    continue;
                } catch (CancelledKeyException cke) {
                    continue;
                }
                if (count == 0) {
                    continue;
                }
                Set<SelectionKey> keys = mSelector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    try {
                        if (key.attachment() instanceof Client) {
                            processClientActivity(key);
                        }
                        else if (key.attachment() instanceof Debugger) {
                            processDebuggerActivity(key);
                        }
                        else if (key.attachment() instanceof MonitorThread) {
                            processDebugSelectedActivity(key);
                        }
                        else {
                            Log.e("ddms", "unknown activity key");
                        }
                    } catch (Exception e) {
                        Log.e("ddms", "Exception during activity from Selector.");
                        Log.e("ddms", e);
                    }
                }
            } catch (Exception e) {
                Log.e("ddms", "Exception MonitorThread.run()");
                Log.e("ddms", e);
            }
        }
    }
    int getDebugSelectedPort() {
        return mDebugSelectedPort;
    }
    private void processClientActivity(SelectionKey key) {
        Client client = (Client)key.attachment();
        try {
            if (key.isReadable() == false || key.isValid() == false) {
                Log.d("ddms", "Invalid key from " + client + ". Dropping client.");
                dropClient(client, true );
                return;
            }
            client.read();
            JdwpPacket packet = client.getJdwpPacket();
            while (packet != null) {
                if (packet.isDdmPacket()) {
                    assert !packet.isReply();
                    callHandler(client, packet, null);
                    packet.consume();
                } else if (packet.isReply()
                        && client.isResponseToUs(packet.getId()) != null) {
                    ChunkHandler handler = client
                            .isResponseToUs(packet.getId());
                    if (packet.isError())
                        client.packetFailed(packet);
                    else if (packet.isEmpty())
                        Log.d("ddms", "Got empty reply for 0x"
                                + Integer.toHexString(packet.getId())
                                + " from " + client);
                    else
                        callHandler(client, packet, handler);
                    packet.consume();
                    client.removeRequestId(packet.getId());
                } else {
                    Log.v("ddms", "Forwarding client "
                            + (packet.isReply() ? "reply" : "event") + " 0x"
                            + Integer.toHexString(packet.getId()) + " to "
                            + client.getDebugger());
                    client.forwardPacketToDebugger(packet);
                }
                packet = client.getJdwpPacket();
            }
        } catch (CancelledKeyException e) {
            dropClient(client, true );
        } catch (IOException ex) {
            dropClient(client, true );
        } catch (Exception ex) {
            Log.e("ddms", ex);
            dropClient(client, true );
            if (ex instanceof BufferOverflowException) {
                Log.w("ddms",
                        "Client data packet exceeded maximum buffer size "
                                + client);
            } else {
                Log.e("ddms", ex);
            }
        }
    }
    private void callHandler(Client client, JdwpPacket packet,
            ChunkHandler handler) {
        if (!client.ddmSeen())
            broadcast(CLIENT_READY, client);
        ByteBuffer buf = packet.getPayload();
        int type, length;
        boolean reply = true;
        type = buf.getInt();
        length = buf.getInt();
        if (handler == null) {
            synchronized (mHandlerMap) {
                handler = mHandlerMap.get(type);
                reply = false;
            }
        }
        if (handler == null) {
            Log.w("ddms", "Received unsupported chunk type "
                    + ChunkHandler.name(type) + " (len=" + length + ")");
        } else {
            Log.d("ddms", "Calling handler for " + ChunkHandler.name(type)
                    + " [" + handler + "] (len=" + length + ")");
            ByteBuffer ibuf = buf.slice();
            ByteBuffer roBuf = ibuf.asReadOnlyBuffer(); 
            roBuf.order(ChunkHandler.CHUNK_ORDER);
            synchronized (mClientList) {
                handler.handleChunk(client, type, roBuf, reply, packet.getId());
            }
        }
    }
    synchronized void dropClient(Client client, boolean notify) {
        if (mInstance == null) {
            return;
        }
        synchronized (mClientList) {
            if (mClientList.remove(client) == false) {
                return;
            }
        }
        client.close(notify);
        broadcast(CLIENT_DISCONNECTED, client);
        wakeup();
    }
    private void processDebuggerActivity(SelectionKey key) {
        Debugger dbg = (Debugger)key.attachment();
        try {
            if (key.isAcceptable()) {
                try {
                    acceptNewDebugger(dbg, null);
                } catch (IOException ioe) {
                    Log.w("ddms", "debugger accept() failed");
                    ioe.printStackTrace();
                }
            } else if (key.isReadable()) {
                processDebuggerData(key);
            } else {
                Log.d("ddm-debugger", "key in unknown state");
            }
        } catch (CancelledKeyException cke) {
        }
    }
    private void acceptNewDebugger(Debugger dbg, ServerSocketChannel acceptChan)
            throws IOException {
        synchronized (mClientList) {
            SocketChannel chan;
            if (acceptChan == null)
                chan = dbg.accept();
            else
                chan = dbg.accept(acceptChan);
            if (chan != null) {
                chan.socket().setTcpNoDelay(true);
                wakeup();
                try {
                    chan.register(mSelector, SelectionKey.OP_READ, dbg);
                } catch (IOException ioe) {
                    dbg.closeData();
                    throw ioe;
                } catch (RuntimeException re) {
                    dbg.closeData();
                    throw re;
                }
            } else {
                Log.w("ddms", "ignoring duplicate debugger");
            }
        }
    }
    private void processDebuggerData(SelectionKey key) {
        Debugger dbg = (Debugger)key.attachment();
        try {
            dbg.read();
            JdwpPacket packet = dbg.getJdwpPacket();
            while (packet != null) {
                Log.v("ddms", "Forwarding dbg req 0x"
                        + Integer.toHexString(packet.getId()) + " to "
                        + dbg.getClient());
                dbg.forwardPacketToClient(packet);
                packet = dbg.getJdwpPacket();
            }
        } catch (IOException ioe) {
            Log.d("ddms", "Closing connection to debugger " + dbg);
            dbg.closeData();
            Client client = dbg.getClient();
            if (client.isDdmAware()) {
                Log.d("ddms", " (recycling client connection as well)");
                client.getDeviceImpl().getMonitor().addClientToDropAndReopen(client,
                        IDebugPortProvider.NO_STATIC_PORT);
            } else {
                Log.d("ddms", " (recycling client connection as well)");
                client.getDeviceImpl().getMonitor().addClientToDropAndReopen(client,
                        IDebugPortProvider.NO_STATIC_PORT);
            }
        }
    }
    private void wakeup() {
        mSelector.wakeup();
    }
    synchronized void quit() {
        mQuit = true;
        wakeup();
        Log.d("ddms", "Waiting for Monitor thread");
        try {
            this.join();
            synchronized (mClientList) {
                for (Client c : mClientList) {
                    c.close(false );
                    broadcast(CLIENT_DISCONNECTED, c);
                }
                mClientList.clear();
            }
            if (mDebugSelectedChan != null) {
                mDebugSelectedChan.close();
                mDebugSelectedChan.socket().close();
                mDebugSelectedChan = null;
            }
            mSelector.close();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInstance = null;
    }
    synchronized void addClient(Client client) {
        if (mInstance == null) {
            return;
        }
        Log.d("ddms", "Adding new client " + client);
        synchronized (mClientList) {
            mClientList.add(client);
            try {
                wakeup();
                client.register(mSelector);
                Debugger dbg = client.getDebugger();
                if (dbg != null) {
                    dbg.registerListener(mSelector);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    private void broadcast(int event, Client client) {
        Log.d("ddms", "broadcast " + event + ": " + client);
        HashSet<ChunkHandler> set;
        synchronized (mHandlerMap) {
            Collection<ChunkHandler> values = mHandlerMap.values();
            set = new HashSet<ChunkHandler>(values);
        }
        Iterator<ChunkHandler> iter = set.iterator();
        while (iter.hasNext()) {
            ChunkHandler handler = iter.next();
            switch (event) {
                case CLIENT_READY:
                    try {
                        handler.clientReady(client);
                    } catch (IOException ioe) {
                        Log.w("ddms",
                                "Got exception while broadcasting 'ready'");
                        return;
                    }
                    break;
                case CLIENT_DISCONNECTED:
                    handler.clientDisconnected(client);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
    private boolean reopenDebugSelectedPort() throws IOException {
        Log.d("ddms", "reopen debug-selected port: " + mNewDebugSelectedPort);
        if (mDebugSelectedChan != null) {
            mDebugSelectedChan.close();
        }
        mDebugSelectedChan = ServerSocketChannel.open();
        mDebugSelectedChan.configureBlocking(false); 
        InetSocketAddress addr = new InetSocketAddress(
                InetAddress.getByName("localhost"), 
                mNewDebugSelectedPort);
        mDebugSelectedChan.socket().setReuseAddress(true); 
        try {
            mDebugSelectedChan.socket().bind(addr);
            if (mSelectedClient != null) {
                mSelectedClient.update(Client.CHANGE_PORT);
            }
            mDebugSelectedChan.register(mSelector, SelectionKey.OP_ACCEPT, this);
            return true;
        } catch (java.net.BindException e) {
            displayDebugSelectedBindError(mNewDebugSelectedPort);
            mDebugSelectedChan = null;
            mNewDebugSelectedPort = -1;
            return false;
        }
    }
    private void processDebugSelectedActivity(SelectionKey key) {
        assert key.isAcceptable();
        ServerSocketChannel acceptChan = (ServerSocketChannel)key.channel();
        if (mSelectedClient != null) {
            Debugger dbg = mSelectedClient.getDebugger();
            if (dbg != null) {
                Log.d("ddms", "Accepting connection on 'debug selected' port");
                try {
                    acceptNewDebugger(dbg, acceptChan);
                } catch (IOException ioe) {
                }
                return;
            }
        }
        Log.w("ddms",
                "Connection on 'debug selected' port, but none selected");
        try {
            SocketChannel chan = acceptChan.accept();
            chan.close();
        } catch (IOException ioe) {
        } catch (NotYetBoundException e) {
            displayDebugSelectedBindError(mDebugSelectedPort);
        }
    }
    private void displayDebugSelectedBindError(int port) {
        String message = String.format(
                "Could not open Selected VM debug port (%1$d). Make sure you do not have another instance of DDMS or of the eclipse plugin running. If it's being used by something else, choose a new port number in the preferences.",
                port);
        Log.logAndDisplay(LogLevel.ERROR, "ddms", message);
    }
}

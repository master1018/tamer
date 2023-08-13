final class ConnectionMultiplexer {
    static int logLevel = LogStream.parseLevel(getLogLevel());
    private static String getLogLevel() {
        return java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.rmi.transport.tcp.multiplex.logLevel"));
    }
    static final Log multiplexLog =
        Log.getLog("sun.rmi.transport.tcp.multiplex",
                   "multiplex", ConnectionMultiplexer.logLevel);
    private final static int OPEN     = 0xE1;
    private final static int CLOSE    = 0xE2;
    private final static int CLOSEACK = 0xE3;
    private final static int REQUEST  = 0xE4;
    private final static int TRANSMIT = 0xE5;
    private TCPChannel channel;
    private InputStream in;
    private OutputStream out;
    private boolean orig;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Hashtable connectionTable = new Hashtable(7);
    private int numConnections = 0;
    private final static int maxConnections = 256;
    private int lastID = 0x1001;
    private boolean alive = true;
    public ConnectionMultiplexer(
        TCPChannel    channel,
        InputStream   in,
        OutputStream  out,
        boolean       orig)
    {
        this.channel = channel;
        this.in      = in;
        this.out     = out;
        this.orig    = orig;
        dataIn = new DataInputStream(in);
        dataOut = new DataOutputStream(out);
    }
    public void run() throws IOException
    {
        try {
            int op, id, length;
            Integer idObj;
            MultiplexConnectionInfo info;
            while (true) {
                op = dataIn.readUnsignedByte();
                switch (op) {
                case OPEN:
                    id = dataIn.readUnsignedShort();
                    if (multiplexLog.isLoggable(Log.VERBOSE)) {
                        multiplexLog.log(Log.VERBOSE, "operation  OPEN " + id);
                    }
                    idObj = new Integer(id);
                    info =
                        (MultiplexConnectionInfo) connectionTable.get(idObj);
                    if (info != null)
                        throw new IOException(
                            "OPEN: Connection ID already exists");
                    info = new MultiplexConnectionInfo(id);
                    info.in = new MultiplexInputStream(this, info, 2048);
                    info.out = new MultiplexOutputStream(this, info, 2048);
                    synchronized (connectionTable) {
                        connectionTable.put(idObj, info);
                        ++ numConnections;
                    }
                    sun.rmi.transport.Connection conn;
                    conn = new TCPConnection(channel, info.in, info.out);
                    channel.acceptMultiplexConnection(conn);
                    break;
                case CLOSE:
                    id = dataIn.readUnsignedShort();
                    if (multiplexLog.isLoggable(Log.VERBOSE)) {
                        multiplexLog.log(Log.VERBOSE, "operation  CLOSE " + id);
                    }
                    idObj = new Integer(id);
                    info =
                        (MultiplexConnectionInfo) connectionTable.get(idObj);
                    if (info == null)
                        throw new IOException(
                            "CLOSE: Invalid connection ID");
                    info.in.disconnect();
                    info.out.disconnect();
                    if (!info.closed)
                        sendCloseAck(info);
                    synchronized (connectionTable) {
                        connectionTable.remove(idObj);
                        -- numConnections;
                    }
                    break;
                case CLOSEACK:
                    id = dataIn.readUnsignedShort();
                    if (multiplexLog.isLoggable(Log.VERBOSE)) {
                        multiplexLog.log(Log.VERBOSE,
                            "operation  CLOSEACK " + id);
                    }
                    idObj = new Integer(id);
                    info =
                        (MultiplexConnectionInfo) connectionTable.get(idObj);
                    if (info == null)
                        throw new IOException(
                            "CLOSEACK: Invalid connection ID");
                    if (!info.closed)
                        throw new IOException(
                            "CLOSEACK: Connection not closed");
                    info.in.disconnect();
                    info.out.disconnect();
                    synchronized (connectionTable) {
                        connectionTable.remove(idObj);
                        -- numConnections;
                    }
                    break;
                case REQUEST:
                    id = dataIn.readUnsignedShort();
                    idObj = new Integer(id);
                    info =
                        (MultiplexConnectionInfo) connectionTable.get(idObj);
                    if (info == null)
                        throw new IOException(
                            "REQUEST: Invalid connection ID");
                    length = dataIn.readInt();
                    if (multiplexLog.isLoggable(Log.VERBOSE)) {
                        multiplexLog.log(Log.VERBOSE,
                            "operation  REQUEST " + id + ": " + length);
                    }
                    info.out.request(length);
                    break;
                case TRANSMIT:
                    id = dataIn.readUnsignedShort();
                    idObj = new Integer(id);
                    info =
                        (MultiplexConnectionInfo) connectionTable.get(idObj);
                    if (info == null)
                        throw new IOException("SEND: Invalid connection ID");
                    length = dataIn.readInt();
                    if (multiplexLog.isLoggable(Log.VERBOSE)) {
                        multiplexLog.log(Log.VERBOSE,
                            "operation  TRANSMIT " + id + ": " + length);
                    }
                    info.in.receive(length, dataIn);
                    break;
                default:
                    throw new IOException("Invalid operation: " +
                                          Integer.toHexString(op));
                }
            }
        } finally {
            shutDown();
        }
    }
    public synchronized TCPConnection openConnection() throws IOException
    {
        int id;
        Integer idObj;
        do {
            lastID = (++ lastID) & 0x7FFF;
            id = lastID;
            if (orig)
                id |= 0x8000;
            idObj = new Integer(id);
        } while (connectionTable.get(idObj) != null);
        MultiplexConnectionInfo info = new MultiplexConnectionInfo(id);
        info.in = new MultiplexInputStream(this, info, 2048);
        info.out = new MultiplexOutputStream(this, info, 2048);
        synchronized (connectionTable) {
            if (!alive)
                throw new IOException("Multiplexer connection dead");
            if (numConnections >= maxConnections)
                throw new IOException("Cannot exceed " + maxConnections +
                    " simultaneous multiplexed connections");
            connectionTable.put(idObj, info);
            ++ numConnections;
        }
        synchronized (dataOut) {
            try {
                dataOut.writeByte(OPEN);
                dataOut.writeShort(id);
                dataOut.flush();
            } catch (IOException e) {
                multiplexLog.log(Log.BRIEF, "exception: ", e);
                shutDown();
                throw e;
            }
        }
        return new TCPConnection(channel, info.in, info.out);
    }
    public void shutDown()
    {
        synchronized (connectionTable) {
            if (!alive)
                return;
            alive = false;
            Enumeration enum_ = connectionTable.elements();
            while (enum_.hasMoreElements()) {
                MultiplexConnectionInfo info =
                    (MultiplexConnectionInfo) enum_.nextElement();
                info.in.disconnect();
                info.out.disconnect();
            }
            connectionTable.clear();
            numConnections = 0;
        }
        try {
            in.close();
        } catch (IOException e) {
        }
        try {
            out.close();
        } catch (IOException e) {
        }
    }
    void sendRequest(MultiplexConnectionInfo info, int len) throws IOException
    {
        synchronized (dataOut) {
            if (alive && !info.closed)
                try {
                    dataOut.writeByte(REQUEST);
                    dataOut.writeShort(info.id);
                    dataOut.writeInt(len);
                    dataOut.flush();
                } catch (IOException e) {
                    multiplexLog.log(Log.BRIEF, "exception: ", e);
                    shutDown();
                    throw e;
                }
        }
    }
    void sendTransmit(MultiplexConnectionInfo info,
                      byte buf[], int off, int len) throws IOException
    {
        synchronized (dataOut) {
            if (alive && !info.closed)
                try {
                    dataOut.writeByte(TRANSMIT);
                    dataOut.writeShort(info.id);
                    dataOut.writeInt(len);
                    dataOut.write(buf, off, len);
                    dataOut.flush();
                } catch (IOException e) {
                    multiplexLog.log(Log.BRIEF, "exception: ", e);
                    shutDown();
                    throw e;
                }
        }
    }
    void sendClose(MultiplexConnectionInfo info) throws IOException
    {
        info.out.disconnect();
        synchronized (dataOut) {
            if (alive && !info.closed)
                try {
                    dataOut.writeByte(CLOSE);
                    dataOut.writeShort(info.id);
                    dataOut.flush();
                    info.closed = true;
                } catch (IOException e) {
                    multiplexLog.log(Log.BRIEF, "exception: ", e);
                    shutDown();
                    throw e;
                }
        }
    }
    void sendCloseAck(MultiplexConnectionInfo info) throws IOException
    {
        synchronized (dataOut) {
            if (alive && !info.closed)
                try {
                    dataOut.writeByte(CLOSEACK);
                    dataOut.writeShort(info.id);
                    dataOut.flush();
                    info.closed = true;
                } catch (IOException e) {
                    multiplexLog.log(Log.BRIEF, "exception: ", e);
                    shutDown();
                    throw e;
                }
        }
    }
    protected void finalize() throws Throwable
    {
        super.finalize();
        shutDown();
    }
}

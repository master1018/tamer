class ConnectionOutputStream extends MarshalOutputStream {
    private final Connection conn;
    private final boolean resultStream;
    private final UID ackID;
    private DGCAckHandler dgcAckHandler = null;
    ConnectionOutputStream(Connection conn, boolean resultStream)
        throws IOException
    {
        super(conn.getOutputStream());
        this.conn = conn;
        this.resultStream = resultStream;
        ackID = resultStream ? new UID() : null;
    }
    void writeID() throws IOException {
        assert resultStream;
        ackID.write(this);
    }
    boolean isResultStream() {
        return resultStream;
    }
    void saveObject(Object obj) {
        if (dgcAckHandler == null) {
            dgcAckHandler = new DGCAckHandler(ackID);
        }
        dgcAckHandler.add(obj);
    }
    DGCAckHandler getDGCAckHandler() {
        return dgcAckHandler;
    }
    void done() {
        if (dgcAckHandler != null) {
            dgcAckHandler.startTimer();
        }
    }
}

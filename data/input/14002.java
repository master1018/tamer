class ConnectionInputStream extends MarshalInputStream {
    private boolean dgcAckNeeded = false;
    private Map incomingRefTable = new HashMap(5);
    private UID ackID;
    ConnectionInputStream(InputStream in) throws IOException {
        super(in);
    }
    void readID() throws IOException {
        ackID = UID.read((DataInput) this);
    }
    void saveRef(LiveRef ref) {
        Endpoint ep = ref.getEndpoint();
        List refList = (List) incomingRefTable.get(ep);
        if (refList == null) {
            refList = new ArrayList();
            incomingRefTable.put(ep, refList);
        }
        refList.add(ref);
    }
    void registerRefs() throws IOException {
        if (!incomingRefTable.isEmpty()) {
            Set entrySet = incomingRefTable.entrySet();
            Iterator iter = entrySet.iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Endpoint ep = (Endpoint) entry.getKey();
                List refList = (List) entry.getValue();
                DGCClient.registerRefs(ep, refList);
            }
        }
    }
    void setAckNeeded() {
        dgcAckNeeded = true;
    }
    void done(Connection c) {
        if (dgcAckNeeded) {
            Connection conn = null;
            Channel ch = null;
            boolean reuse = true;
            DGCImpl.dgcLog.log(Log.VERBOSE, "send ack");
            try {
                ch = c.getChannel();
                conn = ch.newConnection();
                DataOutputStream out =
                    new DataOutputStream(conn.getOutputStream());
                out.writeByte(TransportConstants.DGCAck);
                if (ackID == null) {
                    ackID = new UID();
                }
                ackID.write((DataOutput) out);
                conn.releaseOutputStream();
                conn.getInputStream().available();
                conn.releaseInputStream();
            } catch (RemoteException e) {
                reuse = false;
            } catch (IOException e) {
                reuse = false;
            }
            try {
                if (conn != null)
                    ch.free(conn, reuse);
            } catch (RemoteException e){
            }
        }
    }
}

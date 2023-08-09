class ImpsTransaction {
    private String mId;
    private ImpsConnection mConnection;
    protected ImpsTransaction() {
    }
    void setTransactionInfo(String id, ImpsConnection conn) {
        mId = id;
        mConnection = conn;
    }
    public String getId() {
        return mId;
    }
    protected void sendPrimitive(Primitive primitive) {
        ImpsSession session = mConnection.getSession();
        if (session != null) {
            primitive.setSession(session.getID());
        }
        primitive.setTransaction(this);
        mConnection.sendPrimitive(primitive);
    }
}

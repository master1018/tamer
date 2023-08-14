final class HandleWait extends ChunkHandler {
    public static final int CHUNK_WAIT = ChunkHandler.type("WAIT");
    private static final HandleWait mInst = new HandleWait();
    private HandleWait() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_WAIT, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        Log.d("ddm-wait", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_WAIT) {
            assert !isReply;
            handleWAIT(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    private static void handleWAIT(Client client, ByteBuffer data) {
        byte reason;
        reason = data.get();
        Log.d("ddm-wait", "WAIT: reason=" + reason);
        ClientData cd = client.getClientData();
        synchronized (cd) {
            cd.setDebuggerConnectionStatus(DebuggerStatus.WAITING);
        }
        client.update(Client.CHANGE_DEBUGGER_STATUS);
    }
}

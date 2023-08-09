final class HandleTest extends ChunkHandler {
    public static final int CHUNK_TEST = type("TEST");
    private static final HandleTest mInst = new HandleTest();
    private HandleTest() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_TEST, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        Log.d("ddm-test", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_TEST) {
            handleTEST(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    private void handleTEST(Client client, ByteBuffer data)
    {
        byte[] copy = new byte[data.limit()];
        data.get(copy);
        Log.d("ddm-test", "Received:");
        Log.hexDump("ddm-test", LogLevel.DEBUG, copy, 0, copy.length);
    }
}

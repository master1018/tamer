final class HandleExit extends ChunkHandler {
    public static final int CHUNK_EXIT = type("EXIT");
    private static final HandleExit mInst = new HandleExit();
    private HandleExit() {}
    public static void register(MonitorThread mt) {}
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        handleUnknownChunk(client, type, data, isReply, msgId);
    }
    public static void sendEXIT(Client client, int status)
        throws IOException
    {
        ByteBuffer rawBuf = allocBuffer(4);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.putInt(status);
        finishChunkPacket(packet, CHUNK_EXIT, buf.position());
        Log.d("ddm-exit", "Sending " + name(CHUNK_EXIT) + ": " + status);
        client.sendAndConsume(packet, mInst);
    }
}

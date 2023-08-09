final class HandleProfiling extends ChunkHandler {
    public static final int CHUNK_MPRS = type("MPRS");
    public static final int CHUNK_MPRE = type("MPRE");
    public static final int CHUNK_MPSS = type("MPSS");
    public static final int CHUNK_MPSE = type("MPSE");
    public static final int CHUNK_MPRQ = type("MPRQ");
    public static final int CHUNK_FAIL = type("FAIL");
    private static final HandleProfiling mInst = new HandleProfiling();
    private HandleProfiling() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_MPRE, mInst);
        mt.registerChunkHandler(CHUNK_MPSE, mInst);
        mt.registerChunkHandler(CHUNK_MPRQ, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data,
        boolean isReply, int msgId) {
        Log.d("ddm-prof", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_MPRE) {
            handleMPRE(client, data);
        } else if (type == CHUNK_MPSE) {
            handleMPSE(client, data);
        } else if (type == CHUNK_MPRQ) {
            handleMPRQ(client, data);
        } else if (type == CHUNK_FAIL) {
            handleFAIL(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    public static void sendMPRS(Client client, String fileName, int bufferSize,
        int flags) throws IOException {
        ByteBuffer rawBuf = allocBuffer(3*4 + fileName.length() * 2);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.putInt(bufferSize);
        buf.putInt(flags);
        buf.putInt(fileName.length());
        putString(buf, fileName);
        finishChunkPacket(packet, CHUNK_MPRS, buf.position());
        Log.d("ddm-prof", "Sending " + name(CHUNK_MPRS) + " '" + fileName
            + "', size=" + bufferSize + ", flags=" + flags);
        client.sendAndConsume(packet, mInst);
        client.getClientData().setPendingMethodProfiling(fileName);
        sendMPRQ(client);
    }
    public static void sendMPRE(Client client) throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_MPRE, buf.position());
        Log.d("ddm-prof", "Sending " + name(CHUNK_MPRE));
        client.sendAndConsume(packet, mInst);
    }
    private void handleMPRE(Client client, ByteBuffer data) {
        byte result;
        String filename = client.getClientData().getPendingMethodProfiling();
        client.getClientData().setPendingMethodProfiling(null);
        result = data.get();
        IMethodProfilingHandler handler = ClientData.getMethodProfilingHandler();
        if (handler != null) {
            if (result == 0) {
                handler.onSuccess(filename, client);
                Log.d("ddm-prof", "Method profiling has finished");
            } else {
                handler.onEndFailure(client, null );
                Log.w("ddm-prof", "Method profiling has failed (check device log)");
            }
        }
        client.getClientData().setMethodProfilingStatus(MethodProfilingStatus.OFF);
        client.update(Client.CHANGE_METHOD_PROFILING_STATUS);
    }
    public static void sendMPSS(Client client, int bufferSize,
        int flags) throws IOException {
        ByteBuffer rawBuf = allocBuffer(2*4);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.putInt(bufferSize);
        buf.putInt(flags);
        finishChunkPacket(packet, CHUNK_MPSS, buf.position());
        Log.d("ddm-prof", "Sending " + name(CHUNK_MPSS)
            + "', size=" + bufferSize + ", flags=" + flags);
        client.sendAndConsume(packet, mInst);
        sendMPRQ(client);
    }
    public static void sendMPSE(Client client) throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_MPSE, buf.position());
        Log.d("ddm-prof", "Sending " + name(CHUNK_MPSE));
        client.sendAndConsume(packet, mInst);
    }
    private void handleMPSE(Client client, ByteBuffer data) {
        IMethodProfilingHandler handler = ClientData.getMethodProfilingHandler();
        if (handler != null) {
            byte[] stuff = new byte[data.capacity()];
            data.get(stuff, 0, stuff.length);
            Log.d("ddm-prof", "got trace file, size: " + stuff.length + " bytes");
            handler.onSuccess(stuff, client);
        }
        client.getClientData().setMethodProfilingStatus(MethodProfilingStatus.OFF);
        client.update(Client.CHANGE_METHOD_PROFILING_STATUS);
    }
    public static void sendMPRQ(Client client) throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_MPRQ, buf.position());
        Log.d("ddm-prof", "Sending " + name(CHUNK_MPRQ));
        client.sendAndConsume(packet, mInst);
    }
    private void handleMPRQ(Client client, ByteBuffer data) {
        byte result;
        result = data.get();
        if (result == 0) {
            client.getClientData().setMethodProfilingStatus(MethodProfilingStatus.OFF);
            Log.d("ddm-prof", "Method profiling is not running");
        } else {
            client.getClientData().setMethodProfilingStatus(MethodProfilingStatus.ON);
            Log.d("ddm-prof", "Method profiling is running");
        }
        client.update(Client.CHANGE_METHOD_PROFILING_STATUS);
    }
    private void handleFAIL(Client client, ByteBuffer data) {
         data.getInt();
        int length = data.getInt() * 2;
        String message = null;
        if (length > 0) {
            byte[] messageBuffer = new byte[length];
            data.get(messageBuffer, 0, length);
            message = new String(messageBuffer);
        }
        String filename = client.getClientData().getPendingMethodProfiling();
        if (filename != null) {
            client.getClientData().setPendingMethodProfiling(null);
            IMethodProfilingHandler handler = ClientData.getMethodProfilingHandler();
            if (handler != null) {
                handler.onStartFailure(client, message);
            }
        } else {
            IMethodProfilingHandler handler = ClientData.getMethodProfilingHandler();
            if (handler != null) {
                handler.onEndFailure(client, message);
            }
        }
        try {
            sendMPRQ(client);
        } catch (IOException e) {
            Log.e("HandleProfiling", e);
        }
    }
}

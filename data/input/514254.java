final class HandleThread extends ChunkHandler {
    public static final int CHUNK_THEN = type("THEN");
    public static final int CHUNK_THCR = type("THCR");
    public static final int CHUNK_THDE = type("THDE");
    public static final int CHUNK_THST = type("THST");
    public static final int CHUNK_THNM = type("THNM");
    public static final int CHUNK_STKL = type("STKL");
    private static final HandleThread mInst = new HandleThread();
    private static volatile boolean mThreadStatusReqRunning = false;
    private static volatile boolean mThreadStackTraceReqRunning = false;
    private HandleThread() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_THCR, mInst);
        mt.registerChunkHandler(CHUNK_THDE, mInst);
        mt.registerChunkHandler(CHUNK_THST, mInst);
        mt.registerChunkHandler(CHUNK_THNM, mInst);
        mt.registerChunkHandler(CHUNK_STKL, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {
        Log.d("ddm-thread", "Now ready: " + client);
        if (client.isThreadUpdateEnabled())
            sendTHEN(client, true);
    }
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data, boolean isReply, int msgId) {
        Log.d("ddm-thread", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_THCR) {
            handleTHCR(client, data);
        } else if (type == CHUNK_THDE) {
            handleTHDE(client, data);
        } else if (type == CHUNK_THST) {
            handleTHST(client, data);
        } else if (type == CHUNK_THNM) {
            handleTHNM(client, data);
        } else if (type == CHUNK_STKL) {
            handleSTKL(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    private void handleTHCR(Client client, ByteBuffer data) {
        int threadId, nameLen;
        String name;
        threadId = data.getInt();
        nameLen = data.getInt();
        name = getString(data, nameLen);
        Log.v("ddm-thread", "THCR: " + threadId + " '" + name + "'");
        client.getClientData().addThread(threadId, name);
        client.update(Client.CHANGE_THREAD_DATA);
    }
    private void handleTHDE(Client client, ByteBuffer data) {
        int threadId;
        threadId = data.getInt();
        Log.v("ddm-thread", "THDE: " + threadId);
        client.getClientData().removeThread(threadId);
        client.update(Client.CHANGE_THREAD_DATA);
    }
    private void handleTHST(Client client, ByteBuffer data) {
        int headerLen, bytesPerEntry, extraPerEntry;
        int threadCount;
        headerLen = (data.get() & 0xff);
        bytesPerEntry = (data.get() & 0xff);
        threadCount = data.getShort();
        headerLen -= 4;     
        while (headerLen-- > 0)
            data.get();
        extraPerEntry = bytesPerEntry - 18;     
        Log.v("ddm-thread", "THST: threadCount=" + threadCount);
        for (int i = 0; i < threadCount; i++) {
            int threadId, status, tid, utime, stime;
            boolean isDaemon = false;
            threadId = data.getInt();
            status = data.get();
            tid = data.getInt();
            utime = data.getInt();
            stime = data.getInt();
            if (bytesPerEntry >= 18)
                isDaemon = (data.get() != 0);
            Log.v("ddm-thread", "  id=" + threadId
                + ", status=" + status + ", tid=" + tid
                + ", utime=" + utime + ", stime=" + stime);
            ClientData cd = client.getClientData();
            ThreadInfo threadInfo = cd.getThread(threadId);
            if (threadInfo != null)
                threadInfo.updateThread(status, tid, utime, stime, isDaemon);
            else
                Log.d("ddms", "Thread with id=" + threadId + " not found");
            for (int slurp = extraPerEntry; slurp > 0; slurp--)
                data.get();
        }
        client.update(Client.CHANGE_THREAD_DATA);
    }
    private void handleTHNM(Client client, ByteBuffer data) {
        int threadId, nameLen;
        String name;
        threadId = data.getInt();
        nameLen = data.getInt();
        name = getString(data, nameLen);
        Log.v("ddm-thread", "THNM: " + threadId + " '" + name + "'");
        ThreadInfo threadInfo = client.getClientData().getThread(threadId);
        if (threadInfo != null) {
            threadInfo.setThreadName(name);
            client.update(Client.CHANGE_THREAD_DATA);
        } else {
            Log.d("ddms", "Thread with id=" + threadId + " not found");
        }
    }
    private void handleSTKL(Client client, ByteBuffer data) {
        StackTraceElement[] trace;
        int i, threadId, stackDepth;
        @SuppressWarnings("unused")
        int future;
        future = data.getInt();
        threadId = data.getInt();
        Log.v("ddms", "STKL: " + threadId);
        stackDepth = data.getInt();
        trace = new StackTraceElement[stackDepth];
        for (i = 0; i < stackDepth; i++) {
            String className, methodName, fileName;
            int len, lineNumber;
            len = data.getInt();
            className = getString(data, len);
            len = data.getInt();
            methodName = getString(data, len);
            len = data.getInt();
            if (len == 0) {
                fileName = null;
            } else {
                fileName = getString(data, len);
            }
            lineNumber = data.getInt();
            trace[i] = new StackTraceElement(className, methodName, fileName,
                        lineNumber);
        }
        ThreadInfo threadInfo = client.getClientData().getThread(threadId);
        if (threadInfo != null) {
            threadInfo.setStackCall(trace);
            client.update(Client.CHANGE_THREAD_STACKTRACE);
        } else {
            Log.d("STKL", String.format(
                    "Got stackcall for thread %1$d, which does not exists (anymore?).", 
                    threadId));
        }
    }
    public static void sendTHEN(Client client, boolean enable)
        throws IOException {
        ByteBuffer rawBuf = allocBuffer(1);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        if (enable)
            buf.put((byte)1);
        else
            buf.put((byte)0);
        finishChunkPacket(packet, CHUNK_THEN, buf.position());
        Log.d("ddm-thread", "Sending " + name(CHUNK_THEN) + ": " + enable);
        client.sendAndConsume(packet, mInst);
    }
    public static void sendSTKL(Client client, int threadId)
        throws IOException {
        if (false) {
            Log.d("ddm-thread", "would send STKL " + threadId);
            return;
        }
        ByteBuffer rawBuf = allocBuffer(4);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        buf.putInt(threadId);
        finishChunkPacket(packet, CHUNK_STKL, buf.position());
        Log.d("ddm-thread", "Sending " + name(CHUNK_STKL) + ": " + threadId);
        client.sendAndConsume(packet, mInst);
    }
    static void requestThreadUpdate(final Client client) {
        if (client.isDdmAware() && client.isThreadUpdateEnabled()) {
            if (mThreadStatusReqRunning) {
                Log.w("ddms", "Waiting for previous thread update req to finish");
                return;
            }
            new Thread("Thread Status Req") {
                @Override
                public void run() {
                    mThreadStatusReqRunning = true;
                    try {
                        sendTHST(client);
                    } catch (IOException ioe) {
                        Log.d("ddms", "Unable to request thread updates from "
                                + client + ": " + ioe.getMessage());
                    } finally {
                        mThreadStatusReqRunning = false;
                    }
                }
            }.start();
        }
    }
    static void requestThreadStackCallRefresh(final Client client, final int threadId) {
        if (client.isDdmAware() && client.isThreadUpdateEnabled()) {
            if (mThreadStackTraceReqRunning ) {
                Log.w("ddms", "Waiting for previous thread stack call req to finish");
                return;
            }
            new Thread("Thread Status Req") {
                @Override
                public void run() {
                    mThreadStackTraceReqRunning = true;
                    try {
                        sendSTKL(client, threadId);
                    } catch (IOException ioe) {
                        Log.d("ddms", "Unable to request thread stack call updates from "
                                + client + ": " + ioe.getMessage());
                    } finally {
                        mThreadStackTraceReqRunning = false;
                    }
                }
            }.start();
        }
    }
    private static void sendTHST(Client client) throws IOException {
        ByteBuffer rawBuf = allocBuffer(0);
        JdwpPacket packet = new JdwpPacket(rawBuf);
        ByteBuffer buf = getChunkDataBuf(rawBuf);
        finishChunkPacket(packet, CHUNK_THST, buf.position());
        Log.d("ddm-thread", "Sending " + name(CHUNK_THST));
        client.sendAndConsume(packet, mInst);
    }
}

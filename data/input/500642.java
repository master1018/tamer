public class DdmHandleThread extends ChunkHandler {
    public static final int CHUNK_THEN = type("THEN");
    public static final int CHUNK_THCR = type("THCR");
    public static final int CHUNK_THDE = type("THDE");
    public static final int CHUNK_THST = type("THST");
    public static final int CHUNK_STKL = type("STKL");
    private static DdmHandleThread mInstance = new DdmHandleThread();
    private DdmHandleThread() {}
    public static void register() {
        DdmServer.registerHandler(CHUNK_THEN, mInstance);
        DdmServer.registerHandler(CHUNK_THST, mInstance);
        DdmServer.registerHandler(CHUNK_STKL, mInstance);
    }
    public void connected() {}
    public void disconnected() {}
    public Chunk handleChunk(Chunk request) {
        if (Config.LOGV)
            Log.v("ddm-thread", "Handling " + name(request.type) + " chunk");
        int type = request.type;
        if (type == CHUNK_THEN) {
            return handleTHEN(request);
        } else if (type == CHUNK_THST) {
            return handleTHST(request);
        } else if (type == CHUNK_STKL) {
            return handleSTKL(request);
        } else {
            throw new RuntimeException("Unknown packet "
                + ChunkHandler.name(type));
        }
    }
    private Chunk handleTHEN(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        boolean enable = (in.get() != 0);
        DdmVmInternal.threadNotify(enable);
        return null;        
    }
    private Chunk handleTHST(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        byte[] status = DdmVmInternal.getThreadStats();
        if (status != null)
            return new Chunk(CHUNK_THST, status, 0, status.length);
        else
            return createFailChunk(1, "Can't build THST chunk");
    }
    private Chunk handleSTKL(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        int threadId;
        threadId = in.getInt();
        StackTraceElement[] trace = DdmVmInternal.getStackTraceById(threadId);
        if (trace == null) {
            return createFailChunk(1, "Stack trace unavailable");
        } else {
            return createStackChunk(trace, threadId);
        }
    }
    private Chunk createStackChunk(StackTraceElement[] trace, int threadId) {
        int bufferSize = 0;
        bufferSize += 4;            
        bufferSize += 4;            
        bufferSize += 4;            
        for (StackTraceElement elem : trace) {
            bufferSize += 4 + elem.getClassName().length() * 2;
            bufferSize += 4 + elem.getMethodName().length() * 2;
            bufferSize += 4;
            if (elem.getFileName() != null)
                bufferSize += elem.getFileName().length() * 2;
            bufferSize += 4;        
        }
        ByteBuffer out = ByteBuffer.allocate(bufferSize);
        out.putInt(0);
        out.putInt(threadId);
        out.putInt(trace.length);
        for (StackTraceElement elem : trace) {
            out.putInt(elem.getClassName().length());
            putString(out, elem.getClassName());
            out.putInt(elem.getMethodName().length());
            putString(out, elem.getMethodName());
            if (elem.getFileName() != null) {
                out.putInt(elem.getFileName().length());
                putString(out, elem.getFileName());
            } else {
                out.putInt(0);
            }
            out.putInt(elem.getLineNumber());
        }
        return new Chunk(CHUNK_STKL, out);
    }
}

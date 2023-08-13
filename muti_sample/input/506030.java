public class DdmHandleProfiling extends ChunkHandler {
    public static final int CHUNK_MPRS = type("MPRS");
    public static final int CHUNK_MPRE = type("MPRE");
    public static final int CHUNK_MPSS = type("MPSS");
    public static final int CHUNK_MPSE = type("MPSE");
    public static final int CHUNK_MPRQ = type("MPRQ");
    private static DdmHandleProfiling mInstance = new DdmHandleProfiling();
    private DdmHandleProfiling() {}
    public static void register() {
        DdmServer.registerHandler(CHUNK_MPRS, mInstance);
        DdmServer.registerHandler(CHUNK_MPRE, mInstance);
        DdmServer.registerHandler(CHUNK_MPSS, mInstance);
        DdmServer.registerHandler(CHUNK_MPSE, mInstance);
        DdmServer.registerHandler(CHUNK_MPRQ, mInstance);
    }
    public void connected() {}
    public void disconnected() {}
    public Chunk handleChunk(Chunk request) {
        if (Config.LOGV)
            Log.v("ddm-heap", "Handling " + name(request.type) + " chunk");
        int type = request.type;
        if (type == CHUNK_MPRS) {
            return handleMPRS(request);
        } else if (type == CHUNK_MPRE) {
            return handleMPRE(request);
        } else if (type == CHUNK_MPSS) {
            return handleMPSS(request);
        } else if (type == CHUNK_MPSE) {
            return handleMPSE(request);
        } else if (type == CHUNK_MPRQ) {
            return handleMPRQ(request);
        } else {
            throw new RuntimeException("Unknown packet "
                + ChunkHandler.name(type));
        }
    }
    private Chunk handleMPRS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        int bufferSize = in.getInt();
        int flags = in.getInt();
        int len = in.getInt();
        String fileName = getString(in, len);
        if (Config.LOGV)
            Log.v("ddm-heap", "Method profiling start: filename='" + fileName
                + "', size=" + bufferSize + ", flags=" + flags);
        try {
            Debug.startMethodTracing(fileName, bufferSize, flags);
            return null;        
        } catch (RuntimeException re) {
            return createFailChunk(1, re.getMessage());
        }
    }
    private Chunk handleMPRE(Chunk request) {
        byte result;
        try {
            Debug.stopMethodTracing();
            result = 0;
        } catch (RuntimeException re) {
            Log.w("ddm-heap", "Method profiling end failed: "
                + re.getMessage());
            result = 1;
        }
        byte[] reply = { result };
        return new Chunk(CHUNK_MPRE, reply, 0, reply.length);
    }
    private Chunk handleMPSS(Chunk request) {
        ByteBuffer in = wrapChunk(request);
        int bufferSize = in.getInt();
        int flags = in.getInt();
        if (Config.LOGV) {
            Log.v("ddm-heap", "Method prof stream start: size=" + bufferSize
                + ", flags=" + flags);
        }
        try {
            Debug.startMethodTracingDdms(bufferSize, flags);
            return null;        
        } catch (RuntimeException re) {
            return createFailChunk(1, re.getMessage());
        }
    }
    private Chunk handleMPSE(Chunk request) {
        byte result;
        if (Config.LOGV) {
            Log.v("ddm-heap", "Method prof stream end");
        }
        try {
            Debug.stopMethodTracing();
            result = 0;
        } catch (RuntimeException re) {
            Log.w("ddm-heap", "Method prof stream end failed: "
                + re.getMessage());
            return createFailChunk(1, re.getMessage());
        }
        return null;
    }
    private Chunk handleMPRQ(Chunk request) {
        int result = Debug.isMethodTracingActive() ? 1 : 0;
        byte[] reply = { (byte) result };
        return new Chunk(CHUNK_MPRQ, reply, 0, reply.length);
    }
}

public class DdmHandleExit extends ChunkHandler {
    public static final int CHUNK_EXIT = type("EXIT");
    private static DdmHandleExit mInstance = new DdmHandleExit();
    private DdmHandleExit() {}
    public static void register() {
        DdmServer.registerHandler(CHUNK_EXIT, mInstance);
    }
    public void connected() {}
    public void disconnected() {}
    public Chunk handleChunk(Chunk request) {
        if (Config.LOGV)
            Log.v("ddm-exit", "Handling " + name(request.type) + " chunk");
        ByteBuffer in = wrapChunk(request);
        int statusCode = in.getInt();
        Runtime.getRuntime().halt(statusCode);
        return null;
    }
}

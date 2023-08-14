public class DdmHandleHello extends ChunkHandler {
    public static final int CHUNK_HELO = type("HELO");
    public static final int CHUNK_WAIT = type("WAIT");
    public static final int CHUNK_FEAT = type("FEAT");
    private static DdmHandleHello mInstance = new DdmHandleHello();
    private DdmHandleHello() {}
    public static void register() {
        DdmServer.registerHandler(CHUNK_HELO, mInstance);
        DdmServer.registerHandler(CHUNK_FEAT, mInstance);
    }
    public void connected() {
        if (Config.LOGV)
            Log.v("ddm-hello", "Connected!");
        if (false) {
            byte[] data = new byte[] { 0, 1, 2, 3, 4, -4, -3, -2, -1, 127 };
            Chunk testChunk =
                new Chunk(ChunkHandler.type("TEST"), data, 1, data.length-2);
            DdmServer.sendChunk(testChunk);
        }
    }
    public void disconnected() {
        if (Config.LOGV)
            Log.v("ddm-hello", "Disconnected!");
    }
    public Chunk handleChunk(Chunk request) {
        if (Config.LOGV)
            Log.v("ddm-heap", "Handling " + name(request.type) + " chunk");
        int type = request.type;
        if (type == CHUNK_HELO) {
            return handleHELO(request);
        } else if (type == CHUNK_FEAT) {
            return handleFEAT(request);
        } else {
            throw new RuntimeException("Unknown packet "
                + ChunkHandler.name(type));
        }
    }
    private Chunk handleHELO(Chunk request) {
        if (false)
            return createFailChunk(123, "This is a test");
        ByteBuffer in = wrapChunk(request);
        int serverProtoVers = in.getInt();
        if (Config.LOGV)
            Log.v("ddm-hello", "Server version is " + serverProtoVers);
        String vmName = System.getProperty("java.vm.name", "?");
        String vmVersion = System.getProperty("java.vm.version", "?");
        String vmIdent = vmName + " v" + vmVersion;
        String appName = DdmHandleAppName.getAppName();
        ByteBuffer out = ByteBuffer.allocate(16
                            + vmIdent.length()*2 + appName.length()*2);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(DdmServer.CLIENT_PROTOCOL_VERSION);
        out.putInt(android.os.Process.myPid());
        out.putInt(vmIdent.length());
        out.putInt(appName.length());
        putString(out, vmIdent);
        putString(out, appName);
        Chunk reply = new Chunk(CHUNK_HELO, out);
        if (Debug.waitingForDebugger())
            sendWAIT(0);
        return reply;
    }
    private Chunk handleFEAT(Chunk request) {
        final String[] features = Debug.getVmFeatureList();
        if (Config.LOGV)
            Log.v("ddm-heap", "Got feature list request");
        int size = 4 + 4 * features.length;
        for (int i = features.length-1; i >= 0; i--)
            size += features[i].length() * 2;
        ByteBuffer out = ByteBuffer.allocate(size);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(features.length);
        for (int i = features.length-1; i >= 0; i--) {
            out.putInt(features[i].length());
            putString(out, features[i]);
        }
        return new Chunk(CHUNK_FEAT, out);
    }
    public static void sendWAIT(int reason) {
        byte[] data = new byte[] { (byte) reason };
        Chunk waitChunk = new Chunk(CHUNK_WAIT, data, 0, 1);
        DdmServer.sendChunk(waitChunk);
    }
}

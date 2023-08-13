public class DdmHandleAppName extends ChunkHandler {
    public static final int CHUNK_APNM = type("APNM");
    private volatile static String mAppName = "";
    private static DdmHandleAppName mInstance = new DdmHandleAppName();
    private DdmHandleAppName() {}
    public static void register() {}
    public void connected() {}
    public void disconnected() {}
    public Chunk handleChunk(Chunk request) {
        return null;
    }
    public static void setAppName(String name) {
        if (name == null || name.length() == 0)
            return;
        mAppName = name;
        sendAPNM(name);
    }
    public static String getAppName() {
        return mAppName;
    }
    private static void sendAPNM(String appName) {
        if (Config.LOGV)
            Log.v("ddm", "Sending app name");
        ByteBuffer out = ByteBuffer.allocate(4 + appName.length()*2);
        out.order(ChunkHandler.CHUNK_ORDER);
        out.putInt(appName.length());
        putString(out, appName);
        Chunk chunk = new Chunk(CHUNK_APNM, out);
        DdmServer.sendChunk(chunk);
    }
}

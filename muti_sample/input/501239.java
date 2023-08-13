public class DdmServer {
    public static final int CLIENT_PROTOCOL_VERSION = 1;
    private static HashMap<Integer,ChunkHandler> mHandlerMap =
        new HashMap<Integer,ChunkHandler>();
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static volatile boolean mRegistrationComplete = false;
    private static boolean mRegistrationTimedOut = false;
    private DdmServer() {}
    public static void registerHandler(int type, ChunkHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        synchronized (mHandlerMap) {
            if (mHandlerMap.get(type) != null)
                throw new RuntimeException("type " + Integer.toHexString(type)
                    + " already registered");
            mHandlerMap.put(type, handler);
        }
    }
    public static ChunkHandler unregisterHandler(int type) {
        synchronized (mHandlerMap) {
            return mHandlerMap.remove(type);
        }
    }
    public static void registrationComplete() {
        synchronized (mHandlerMap) {
            mRegistrationComplete = true;
            mHandlerMap.notifyAll();
        }
    }
    public static void sendChunk(Chunk chunk) {
        nativeSendChunk(chunk.type, chunk.data, chunk.offset, chunk.length);
    }
    native private static void nativeSendChunk(int type, byte[] data,
        int offset, int length);
    private static void broadcast(int event)
    {
        synchronized (mHandlerMap) {
            Collection values = mHandlerMap.values();
            Iterator iter = values.iterator();
            while (iter.hasNext()) {
                ChunkHandler handler = (ChunkHandler) iter.next();
                switch (event) {
                    case CONNECTED:
                        handler.connected();
                        break;
                    case DISCONNECTED:
                        handler.disconnected();
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        }
    }
    private static Chunk dispatch(int type, byte[] data, int offset, int length)
    {
        ChunkHandler handler;
        synchronized (mHandlerMap) {
            while (!mRegistrationComplete && !mRegistrationTimedOut) {
                try {
                    mHandlerMap.wait(1000);     
                } catch (InterruptedException ie) {
                    continue;
                }
                if (!mRegistrationComplete) {
                    mRegistrationTimedOut = true;
                }
            }
            handler = mHandlerMap.get(type);
        }
        if (handler == null) {
            return null;
        }
        Chunk chunk = new Chunk(type, data, offset, length);
        return handler.handleChunk(chunk);
    }
}

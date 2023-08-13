abstract class ChunkHandler {
    public static final int CHUNK_HEADER_LEN = 8;   
    public static final ByteOrder CHUNK_ORDER = ByteOrder.BIG_ENDIAN;
    public static final int CHUNK_FAIL = type("FAIL");
    ChunkHandler() {}
    abstract void clientReady(Client client) throws IOException;
    abstract void clientDisconnected(Client client);
    abstract void handleChunk(Client client, int type,
        ByteBuffer data, boolean isReply, int msgId);
    protected void handleUnknownChunk(Client client, int type,
        ByteBuffer data, boolean isReply, int msgId) {
        if (type == CHUNK_FAIL) {
            int errorCode, msgLen;
            String msg;
            errorCode = data.getInt();
            msgLen = data.getInt();
            msg = getString(data, msgLen);
            Log.w("ddms", "WARNING: failure code=" + errorCode + " msg=" + msg);
        } else {
            Log.w("ddms", "WARNING: received unknown chunk " + name(type)
                + ": len=" + data.limit() + ", reply=" + isReply
                + ", msgId=0x" + Integer.toHexString(msgId));
        }
        Log.w("ddms", "         client " + client + ", handler " + this);
    }
    static String getString(ByteBuffer buf, int len) {
        char[] data = new char[len];
        for (int i = 0; i < len; i++)
            data[i] = buf.getChar();
        return new String(data);
    }
    static void putString(ByteBuffer buf, String str) {
        int len = str.length();
        for (int i = 0; i < len; i++)
            buf.putChar(str.charAt(i));
    }
    static int type(String typeName) {
        int val = 0;
        if (typeName.length() != 4) {
            Log.e("ddms", "Type name must be 4 letter long");
            throw new RuntimeException("Type name must be 4 letter long");
        }
        for (int i = 0; i < 4; i++) {
            val <<= 8;
            val |= (byte) typeName.charAt(i);
        }
        return val;
    }
    static String name(int type) {
        char[] ascii = new char[4];
        ascii[0] = (char) ((type >> 24) & 0xff);
        ascii[1] = (char) ((type >> 16) & 0xff);
        ascii[2] = (char) ((type >> 8) & 0xff);
        ascii[3] = (char) (type & 0xff);
        return new String(ascii);
    }
    static ByteBuffer allocBuffer(int maxChunkLen) {
        ByteBuffer buf =
            ByteBuffer.allocate(JdwpPacket.JDWP_HEADER_LEN + 8 +maxChunkLen);
        buf.order(CHUNK_ORDER);
        return buf;
    }
    static ByteBuffer getChunkDataBuf(ByteBuffer jdwpBuf) {
        ByteBuffer slice;
        assert jdwpBuf.position() == 0;
        jdwpBuf.position(JdwpPacket.JDWP_HEADER_LEN + CHUNK_HEADER_LEN);
        slice = jdwpBuf.slice();
        slice.order(CHUNK_ORDER);
        jdwpBuf.position(0);
        return slice;
    }
    static void finishChunkPacket(JdwpPacket packet, int type, int chunkLen) {
        ByteBuffer buf = packet.getPayload();
        buf.putInt(0x00, type);
        buf.putInt(0x04, chunkLen);
        packet.finishPacket(CHUNK_HEADER_LEN + chunkLen);
    }
    protected static Client checkDebuggerPortForAppName(Client client, String appName) {
        IDebugPortProvider provider = DebugPortManager.getProvider();
        if (provider != null) {
            Device device = client.getDeviceImpl();
            int newPort = provider.getPort(device, appName);
            if (newPort != IDebugPortProvider.NO_STATIC_PORT &&
                    newPort != client.getDebuggerListenPort()) {
                AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
                if (bridge != null) {
                    DeviceMonitor deviceMonitor = bridge.getDeviceMonitor();
                    if (deviceMonitor != null) {
                        deviceMonitor.addClientToDropAndReopen(client, newPort);
                        client = null;
                    }
                }
            }
        }
        return client;
    }
}

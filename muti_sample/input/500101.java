final class HandleAppName extends ChunkHandler {
    public static final int CHUNK_APNM = ChunkHandler.type("APNM");
    private static final HandleAppName mInst = new HandleAppName();
    private HandleAppName() {}
    public static void register(MonitorThread mt) {
        mt.registerChunkHandler(CHUNK_APNM, mInst);
    }
    @Override
    public void clientReady(Client client) throws IOException {}
    @Override
    public void clientDisconnected(Client client) {}
    @Override
    public void handleChunk(Client client, int type, ByteBuffer data,
            boolean isReply, int msgId) {
        Log.d("ddm-appname", "handling " + ChunkHandler.name(type));
        if (type == CHUNK_APNM) {
            assert !isReply;
            handleAPNM(client, data);
        } else {
            handleUnknownChunk(client, type, data, isReply, msgId);
        }
    }
    private static void handleAPNM(Client client, ByteBuffer data) {
        int appNameLen;
        String appName;
        appNameLen = data.getInt();
        appName = getString(data, appNameLen);
        Log.d("ddm-appname", "APNM: app='" + appName + "'");
        ClientData cd = client.getClientData();
        synchronized (cd) {
            cd.setClientDescription(appName);
        }
        client = checkDebuggerPortForAppName(client, appName);
        if (client != null) {
            client.update(Client.CHANGE_NAME);
        }
    }
 }

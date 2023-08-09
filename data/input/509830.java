public class PingParser extends Parser {
    private ArrayList<String> syncList = new ArrayList<String>();
    private EasSyncService mService;
    private int mSyncStatus = 0;
    public ArrayList<String> getSyncList() {
        return syncList;
    }
    public int getSyncStatus() {
        return mSyncStatus;
    }
    public PingParser(InputStream in, EasSyncService service) throws IOException {
        super(in);
        mService = service;
    }
    public void parsePingFolders(ArrayList<String> syncList) throws IOException {
        while (nextTag(Tags.PING_FOLDERS) != END) {
            if (tag == Tags.PING_FOLDER) {
                String serverId = getValue();
                syncList.add(serverId);
                mService.userLog("Changes found in: ", serverId);
            } else {
                skipTag();
            }
        }
    }
    @Override
    public boolean parse() throws IOException, StaleFolderListException, IllegalHeartbeatException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != Tags.PING_PING) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == Tags.PING_STATUS) {
                int status = getValueInt();
                mSyncStatus = status;
                mService.userLog("Ping completed, status = ", status);
                if (status == 2) {
                    res = true;
                } else if (status == 7 || status == 4) {
                    throw new StaleFolderListException();
                } else if (status == 5) {
                }
            } else if (tag == Tags.PING_FOLDERS) {
                parsePingFolders(syncList);
            } else if (tag == Tags.PING_HEARTBEAT_INTERVAL) {
                throw new IllegalHeartbeatException(getValueInt());
            } else {
                skipTag();
            }
        }
        return res;
    }
}

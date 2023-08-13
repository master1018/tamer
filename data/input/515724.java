public class PrefManager {
    private ArrayList<ConnectionConfig> mActiveConnectionPrefs;
    private static PrefManager sInstance;
    private PrefManager() {
        mActiveConnectionPrefs = new ArrayList<ConnectionConfig>();
        mActiveConnectionPrefs.add(new ImpsConnectionConfig());
    }
    public synchronized static PrefManager getInstance() {
        if (sInstance == null) {
            sInstance = new PrefManager();
        }
        return sInstance;
    }
    public ArrayList<ConnectionConfig> getActiveConnectionPrefs() {
        return mActiveConnectionPrefs;
    }
}

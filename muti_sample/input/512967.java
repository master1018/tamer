public class BluetoothOppPreference {
    private static final String TAG = "BluetoothOppPreference";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private static BluetoothOppPreference INSTANCE;
    private static Object INSTANCE_LOCK = new Object();
    private boolean mInitialized;
    private Context mContext;
    private SharedPreferences mNamePreference;
    private SharedPreferences mChannelPreference;
    private HashMap<String, Integer> mChannels = new HashMap<String, Integer>();
    private HashMap<String, String> mNames = new HashMap<String, String>();
    public static BluetoothOppPreference getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new BluetoothOppPreference();
            }
            if (!INSTANCE.init(context)) {
                return null;
            }
            return INSTANCE;
        }
    }
    private boolean init(Context context) {
        if (mInitialized)
            return true;
        mInitialized = true;
        mContext = context;
        mNamePreference = mContext.getSharedPreferences(Constants.BLUETOOTHOPP_NAME_PREFERENCE,
                Context.MODE_PRIVATE);
        mChannelPreference = mContext.getSharedPreferences(
                Constants.BLUETOOTHOPP_CHANNEL_PREFERENCE, Context.MODE_PRIVATE);
        mNames = (HashMap<String, String>) mNamePreference.getAll();
        mChannels = (HashMap<String, Integer>) mChannelPreference.getAll();
        return true;
    }
    private String getChannelKey(BluetoothDevice remoteDevice, int uuid) {
        return remoteDevice.getAddress() + "_" + Integer.toHexString(uuid);
    }
    public String getName(BluetoothDevice remoteDevice) {
        if (remoteDevice.getAddress().equals("FF:FF:FF:00:00:00")) {
            return "localhost";
        }
        if (!mNames.isEmpty()) {
            String name = mNames.get(remoteDevice.getAddress());
            if (name != null) {
                return name;
            }
        }
        return null;
    }
    public int getChannel(BluetoothDevice remoteDevice, int uuid) {
        String key = getChannelKey(remoteDevice, uuid);
        if (V) Log.v(TAG, "getChannel " + key);
        Integer channel = null;
        if (mChannels != null) {
            channel = mChannels.get(key);
            if (V) Log.v(TAG, "getChannel for " + remoteDevice + "_" + Integer.toHexString(uuid) +
                        " as " + channel);
        }
        return (channel != null) ? channel : -1;
    }
    public void setName(BluetoothDevice remoteDevice, String name) {
        if (V) Log.v(TAG, "Setname for " + remoteDevice + " to " + name);
        if (!name.equals(getName(remoteDevice))) {
            Editor ed = mNamePreference.edit();
            ed.putString(remoteDevice.getAddress(), name);
            ed.commit();
            mNames.put(remoteDevice.getAddress(), name);
        }
    }
    public void setChannel(BluetoothDevice remoteDevice, int uuid, int channel) {
        if (V) Log.v(TAG, "Setchannel for " + remoteDevice + "_" + Integer.toHexString(uuid) + " to "
                    + channel);
        if (channel != getChannel(remoteDevice, uuid)) {
            String key = getChannelKey(remoteDevice, uuid);
            Editor ed = mChannelPreference.edit();
            ed.putInt(key, channel);
            ed.commit();
            mChannels.put(key, channel);
        }
    }
    public void removeChannel(BluetoothDevice remoteDevice, int uuid) {
        String key = getChannelKey(remoteDevice, uuid);
        Editor ed = mChannelPreference.edit();
        ed.remove(key);
        ed.commit();
        mChannels.remove(key);
    }
    public void dump() {
        Log.d(TAG, "Dumping Names:  ");
        Log.d(TAG, mNames.toString());
        Log.d(TAG, "Dumping Channels:  ");
        Log.d(TAG, mChannels.toString());
    }
}

public class DebugPortProvider implements IDebugPortProvider {
    private static DebugPortProvider sThis  = new DebugPortProvider();
    public static final String PREFS_STATIC_PORT_LIST = "android.staticPortList"; 
    private Map<String, Map<String, Integer>> mMap;
    public static DebugPortProvider getInstance() {
        return sThis;
    }
    private DebugPortProvider() {
        computePortList();
    }
    public int getPort(IDevice device, String appName) {
        if (mMap != null) {
            Map<String, Integer> deviceMap = mMap.get(device.getSerialNumber());
            if (deviceMap != null) {
                Integer i = deviceMap.get(appName);
                if (i != null) {
                    return i.intValue();
                }
            }
        }
        return IDebugPortProvider.NO_STATIC_PORT;
    }
    public Map<String, Map<String, Integer>> getPortList() {
        return mMap;
    }
    private void computePortList() {
        mMap = new HashMap<String, Map<String, Integer>>();
        IPreferenceStore store = PrefsDialog.getStore();
        String value = store.getString(PREFS_STATIC_PORT_LIST);
        if (value != null && value.length() > 0) {
            String[] portSegments = value.split("\\|");  
            for (String seg : portSegments) {
                String[] entry = seg.split(":");  
                String deviceName = null;
                if (entry.length == 3) {
                    deviceName = entry[2];
                } else {
                    deviceName = IDevice.FIRST_EMULATOR_SN;
                }
                Map<String, Integer> deviceMap = mMap.get(deviceName);
                if (deviceMap == null) {
                    deviceMap = new HashMap<String, Integer>();
                    mMap.put(deviceName, deviceMap);
                }
                deviceMap.put(entry[0], Integer.valueOf(entry[1]));
            }
        }
    }
    public void setPortList(Map<String, Map<String,Integer>> map) {
        mMap.clear();
        mMap.putAll(map);
        StringBuilder sb = new StringBuilder();
        Set<String> deviceKeys = map.keySet();
        for (String deviceKey : deviceKeys) {
            Map<String, Integer> deviceMap = map.get(deviceKey);
            if (deviceMap != null) {
                Set<String> appKeys = deviceMap.keySet();
                for (String appKey : appKeys) {
                    Integer port = deviceMap.get(appKey);
                    if (port != null) {
                        sb.append(appKey).append(':').append(port.intValue()).append(':').
                            append(deviceKey).append('|');
                    }
                }
            }
        }
        String value = sb.toString();
        IPreferenceStore store = PrefsDialog.getStore();
        store.setValue(PREFS_STATIC_PORT_LIST, value);
    }
}

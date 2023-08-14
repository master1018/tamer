public class DemoImPlugin extends Service implements ImPlugin {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
   public Map getProviderConfig() {
       HashMap<String, String> config = new HashMap<String, String>();
       config.put(ImConfigNames.PROTOCOL_NAME, "IMPS");
       config.put(ImpsConfigNames.HOST, "http:
       config.put(ImpsConfigNames.CLIENT_ID, "Jimmy");
       config.put(ImpsConfigNames.DATA_CHANNEL, "HTTP");
       config.put(ImpsConfigNames.DATA_ENCODING, "WBXML");
       config.put(ImpsConfigNames.CIR_CHANNEL, "STCP");
       config.put(ImpsConfigNames.CUSTOM_PRESENCE_MAPPING,
               "com.android.im.plugin.demo.DemoPresenceMapping");
       return config;
   }
   public Map getResourceMap() {
       HashMap<Integer, Integer> resMapping = new HashMap<Integer, Integer>();
       return resMapping;
   }
   public int[] getSmileyIconIds() {
       return SMILEY_RES_IDS;
   }
    static final int[] SMILEY_RES_IDS = {
    };
}

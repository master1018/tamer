class VoiceMailConstants {
    private HashMap<String, String[]> CarrierVmMap;
    static final String LOG_TAG = "GSM";
    static final String PARTNER_VOICEMAIL_PATH ="etc/voicemail-conf.xml";
    static final int NAME = 0;
    static final int NUMBER = 1;
    static final int TAG = 2;
    static final int SIZE = 3;
    VoiceMailConstants () {
        CarrierVmMap = new HashMap<String, String[]>();
        loadVoiceMail();
    }
    boolean containsCarrier(String carrier) {
        return CarrierVmMap.containsKey(carrier);
    }
    String getCarrierName(String carrier) {
        String[] data = CarrierVmMap.get(carrier);
        return data[NAME];
    }
    String getVoiceMailNumber(String carrier) {
        String[] data = CarrierVmMap.get(carrier);
        return data[NUMBER];
    }
    String getVoiceMailTag(String carrier) {
        String[] data = CarrierVmMap.get(carrier);
        return data[TAG];
    }
    private void loadVoiceMail() {
        FileReader vmReader;
        final File vmFile = new File(Environment.getRootDirectory(),
                PARTNER_VOICEMAIL_PATH);
        try {
            vmReader = new FileReader(vmFile);
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, "Can't open " +
                    Environment.getRootDirectory() + "/" + PARTNER_VOICEMAIL_PATH);
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(vmReader);
            XmlUtils.beginDocument(parser, "voicemail");
            while (true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (!"voicemail".equals(name)) {
                    break;
                }
                String[] data = new String[SIZE];
                String numeric = parser.getAttributeValue(null, "numeric");
                data[NAME]     = parser.getAttributeValue(null, "carrier");
                data[NUMBER]   = parser.getAttributeValue(null, "vmnumber");
                data[TAG]      = parser.getAttributeValue(null, "vmtag");
                CarrierVmMap.put(numeric, data);
            }
        } catch (XmlPullParserException e) {
            Log.w(LOG_TAG, "Exception in Voicemail parser " + e);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Exception in Voicemail parser " + e);
        }
    }
}

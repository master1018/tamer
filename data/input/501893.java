public class PowerProfile {
    public static final String POWER_NONE = "none";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    public static final String POWER_CPU_ACTIVE = "cpu.active";
    public static final String POWER_WIFI_SCAN = "wifi.scan";
    public static final String POWER_WIFI_ON = "wifi.on";
    public static final String POWER_WIFI_ACTIVE = "wifi.active";
    public static final String POWER_GPS_ON = "gps.on";
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";
    public static final String POWER_SCREEN_ON = "screen.on";
    public static final String POWER_RADIO_ON = "radio.on";
    public static final String POWER_RADIO_SCANNING = "radio.scanning";
    public static final String POWER_RADIO_ACTIVE = "radio.active";
    public static final String POWER_SCREEN_FULL = "screen.full";
    public static final String POWER_AUDIO = "dsp.audio";
    public static final String POWER_VIDEO = "dsp.video";
    public static final String POWER_CPU_SPEEDS = "cpu.speeds";
    static final HashMap<String, Object> sPowerMap = new HashMap<String, Object>();
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ITEM = "item";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String ATTR_NAME = "name";
    public PowerProfile(Context context) {
        if (sPowerMap.size() == 0) {
            readPowerValuesFromXml(context);
        }
    }
    private void readPowerValuesFromXml(Context context) {
        int id = com.android.internal.R.xml.power_profile;
        XmlResourceParser parser = context.getResources().getXml(id);
        boolean parsingArray = false;
        ArrayList<Double> array = new ArrayList<Double>();
        String arrayName = null;
        try {
            XmlUtils.beginDocument(parser, TAG_DEVICE);
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null) break;
                if (parsingArray && !element.equals(TAG_ARRAYITEM)) {
                    sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
                    parsingArray = false;
                }
                if (element.equals(TAG_ARRAY)) {
                    parsingArray = true;
                    array.clear();
                    arrayName = parser.getAttributeValue(null, ATTR_NAME);
                } else if (element.equals(TAG_ITEM) || element.equals(TAG_ARRAYITEM)) {
                    String name = null;
                    if (!parsingArray) name = parser.getAttributeValue(null, ATTR_NAME);
                    if (parser.next() == XmlPullParser.TEXT) {
                        String power = parser.getText();
                        double value = 0;
                        try {
                            value = Double.valueOf(power);
                        } catch (NumberFormatException nfe) {
                        }
                        if (element.equals(TAG_ITEM)) {
                            sPowerMap.put(name, value);
                        } else if (parsingArray) {
                            array.add(value);
                        }
                    }
                }
            }
            if (parsingArray) {
                sPowerMap.put(arrayName, array.toArray(new Double[array.size()]));
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }
    }
    public double getAveragePower(String type) {
        if (sPowerMap.containsKey(type)) {
            Object data = sPowerMap.get(type);
            if (data instanceof Double[]) {
                return ((Double[])data)[0];
            } else {
                return (Double) sPowerMap.get(type);
            }
        } else {
            return 0;
        }
    }
    public double getAveragePower(String type, int level) {
        if (sPowerMap.containsKey(type)) {
            Object data = sPowerMap.get(type);
            if (data instanceof Double[]) {
                final Double[] values = (Double[]) data;
                if (values.length > level && level >= 0) {
                    return values[level];
                } else if (level < 0) {
                    return 0;
                } else {
                    return values[values.length - 1];
                }
            } else {
                return (Double) data;
            }
        } else {
            return 0;
        }
    }
    public int getNumSpeedSteps() {
        Object value = sPowerMap.get(POWER_CPU_SPEEDS);
        if (value != null && value instanceof Double[]) {
            return ((Double[])value).length;
        }
        return 1; 
    }
}

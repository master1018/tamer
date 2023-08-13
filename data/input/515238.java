public class TimeUtils {
    private static final String TAG = "TimeUtils";
    public static TimeZone getTimeZone(int offset, boolean dst, long when, String country) {
        if (country == null) {
            return null;
        }
        TimeZone best = null;
        Resources r = Resources.getSystem();
        XmlResourceParser parser = r.getXml(com.android.internal.R.xml.time_zones_by_country);
        Date d = new Date(when);
        TimeZone current = TimeZone.getDefault();
        String currentName = current.getID();
        int currentOffset = current.getOffset(when);
        boolean currentDst = current.inDaylightTime(d);
        try {
            XmlUtils.beginDocument(parser, "timezones");
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName();
                if (element == null || !(element.equals("timezone"))) {
                    break;
                }
                String code = parser.getAttributeValue(null, "code");
                if (country.equals(code)) {
                    if (parser.next() == XmlPullParser.TEXT) {
                        String maybe = parser.getText();
                        if (maybe.equals(currentName)) {
                            if (currentOffset == offset && currentDst == dst) {
                                return current;
                            }
                        }
                        if (best == null) {
                            TimeZone tz = TimeZone.getTimeZone(maybe);
                            if (tz.getOffset(when) == offset &&
                                tz.inDaylightTime(d) == dst) {
                                best = tz;
                            }
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e);
        } catch (IOException e) {
            Log.e(TAG, "Got exception while getting preferred time zone.", e);
        } finally {
            parser.close();
        }
        return best;
    }
    public static String getTimeZoneDatabaseVersion() {
        return ZoneInfoDB.getVersion();
    }
}

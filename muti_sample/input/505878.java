public class RecordLocationPreference extends IconListPreference {
    public static final String KEY = "pref_camera_recordlocation_key";
    public static final String VALUE_NONE = "none";
    public static final String VALUE_ON = "on";
    public static final String VALUE_OFF = "off";
    private final ContentResolver mResolver;
    public RecordLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResolver = context.getContentResolver();
    }
    @Override
    public String getValue() {
        return get(getSharedPreferences(), mResolver) ? VALUE_ON : VALUE_OFF;
    }
    public static boolean get(
            SharedPreferences pref, ContentResolver resolver) {
        String value = pref.getString(KEY, VALUE_NONE);
        return VALUE_ON.equals(value);
    }
}

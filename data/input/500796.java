public class VCardConfig {
    private static final String LOG_TAG = "VCardConfig";
     static final int LOG_LEVEL_NONE = 0;
     static final int LOG_LEVEL_PERFORMANCE_MEASUREMENT = 0x1;
     static final int LOG_LEVEL_SHOW_WARNING = 0x2;
     static final int LOG_LEVEL_VERBOSE =
        LOG_LEVEL_PERFORMANCE_MEASUREMENT | LOG_LEVEL_SHOW_WARNING;
     static final int LOG_LEVEL = LOG_LEVEL_NONE;
     static final int PARSE_TYPE_UNKNOWN = 0;
     static final int PARSE_TYPE_APPLE = 1;
     static final int PARSE_TYPE_MOBILE_PHONE_JP = 2;  
     static final int PARSE_TYPE_FOMA = 3;  
     static final int PARSE_TYPE_WINDOWS_MOBILE_JP = 4;
    public static final String DEFAULT_CHARSET = "iso-8859-1";
    public static final int FLAG_V21 = 0;
    public static final int FLAG_V30 = 1;
    public static final int NAME_ORDER_DEFAULT = 0;
    public static final int NAME_ORDER_EUROPE = 0x4;
    public static final int NAME_ORDER_JAPANESE = 0x8;
    private static final int NAME_ORDER_MASK = 0xC;
    private static final int FLAG_CHARSET_UTF8 = 0;
    private static final int FLAG_CHARSET_SHIFT_JIS = 0x100;
    private static final int FLAG_CHARSET_MASK = 0xF00;
    private static final int FLAG_USE_ANDROID_PROPERTY = 0x80000000;
    private static final int FLAG_USE_DEFACT_PROPERTY = 0x40000000;
    private static final int FLAG_DOCOMO = 0x20000000;
    public static final int FLAG_REFRAIN_QP_TO_NAME_PROPERTIES = 0x10000000;
    public static final int FLAG_CONVERT_PHONETIC_NAME_STRINGS = 0x0800000;
    public static final int FLAG_APPEND_TYPE_PARAM = 0x04000000;
    public static final int FLAG_REFRAIN_IMAGE_EXPORT = 0x02000000;
    public static final int VCARD_TYPE_V21_GENERIC_UTF8 =
        (FLAG_V21 | NAME_ORDER_DEFAULT | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static String VCARD_TYPE_V21_GENERIC_UTF8_STR = "v21_generic";
    public static final int VCARD_TYPE_V30_GENERIC_UTF8 =
        (FLAG_V30 | NAME_ORDER_DEFAULT | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V30_GENERIC_UTF8_STR = "v30_generic";
    public static final int VCARD_TYPE_V21_EUROPE_UTF8 =
        (FLAG_V21 | NAME_ORDER_EUROPE | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V21_EUROPE_UTF8_STR = "v21_europe";
    public static final int VCARD_TYPE_V30_EUROPE_UTF8 =
        (FLAG_V30 | NAME_ORDER_EUROPE | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V30_EUROPE_STR = "v30_europe";
    public static final int VCARD_TYPE_V21_JAPANESE_UTF8 =
        (FLAG_V21 | NAME_ORDER_JAPANESE | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V21_JAPANESE_UTF8_STR = "v21_japanese_utf8";
    public static final int VCARD_TYPE_V21_JAPANESE_SJIS =
        (FLAG_V21 | NAME_ORDER_JAPANESE | FLAG_CHARSET_SHIFT_JIS |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V21_JAPANESE_SJIS_STR = "v21_japanese_sjis";
    public static final int VCARD_TYPE_V30_JAPANESE_SJIS =
        (FLAG_V30 | NAME_ORDER_JAPANESE | FLAG_CHARSET_SHIFT_JIS |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V30_JAPANESE_SJIS_STR = "v30_japanese_sjis";
    public static final int VCARD_TYPE_V30_JAPANESE_UTF8 =
        (FLAG_V30 | NAME_ORDER_JAPANESE | FLAG_CHARSET_UTF8 |
                FLAG_USE_DEFACT_PROPERTY | FLAG_USE_ANDROID_PROPERTY);
     static final String VCARD_TYPE_V30_JAPANESE_UTF8_STR = "v30_japanese_utf8";
    public static final int VCARD_TYPE_V21_JAPANESE_MOBILE =
        (FLAG_V21 | NAME_ORDER_JAPANESE | FLAG_CHARSET_SHIFT_JIS |
                FLAG_CONVERT_PHONETIC_NAME_STRINGS |
                FLAG_REFRAIN_QP_TO_NAME_PROPERTIES);
     static final String VCARD_TYPE_V21_JAPANESE_MOBILE_STR = "v21_japanese_mobile";
    public static final int VCARD_TYPE_DOCOMO =
        (VCARD_TYPE_V21_JAPANESE_MOBILE | FLAG_DOCOMO);
     static final String VCARD_TYPE_DOCOMO_STR = "docomo";
    public static int VCARD_TYPE_DEFAULT = VCARD_TYPE_V21_GENERIC_UTF8;
    private static final Map<String, Integer> sVCardTypeMap;
    private static final Set<Integer> sJapaneseMobileTypeSet;
    static {
        sVCardTypeMap = new HashMap<String, Integer>();
        sVCardTypeMap.put(VCARD_TYPE_V21_GENERIC_UTF8_STR, VCARD_TYPE_V21_GENERIC_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V30_GENERIC_UTF8_STR, VCARD_TYPE_V30_GENERIC_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V21_EUROPE_UTF8_STR, VCARD_TYPE_V21_EUROPE_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V30_EUROPE_STR, VCARD_TYPE_V30_EUROPE_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V21_JAPANESE_SJIS_STR, VCARD_TYPE_V21_JAPANESE_SJIS);
        sVCardTypeMap.put(VCARD_TYPE_V21_JAPANESE_UTF8_STR, VCARD_TYPE_V21_JAPANESE_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V30_JAPANESE_SJIS_STR, VCARD_TYPE_V30_JAPANESE_SJIS);
        sVCardTypeMap.put(VCARD_TYPE_V30_JAPANESE_UTF8_STR, VCARD_TYPE_V30_JAPANESE_UTF8);
        sVCardTypeMap.put(VCARD_TYPE_V21_JAPANESE_MOBILE_STR, VCARD_TYPE_V21_JAPANESE_MOBILE);
        sVCardTypeMap.put(VCARD_TYPE_DOCOMO_STR, VCARD_TYPE_DOCOMO);
        sJapaneseMobileTypeSet = new HashSet<Integer>();
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V21_JAPANESE_SJIS);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V21_JAPANESE_UTF8);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V21_JAPANESE_SJIS);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V30_JAPANESE_SJIS);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V30_JAPANESE_UTF8);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_V21_JAPANESE_MOBILE);
        sJapaneseMobileTypeSet.add(VCARD_TYPE_DOCOMO);
    }
    public static int getVCardTypeFromString(final String vcardTypeString) {
        final String loweredKey = vcardTypeString.toLowerCase();
        if (sVCardTypeMap.containsKey(loweredKey)) {
            return sVCardTypeMap.get(loweredKey);
        } else if ("default".equalsIgnoreCase(vcardTypeString)) {
            return VCARD_TYPE_DEFAULT;
        } else {
            Log.e(LOG_TAG, "Unknown vCard type String: \"" + vcardTypeString + "\"");
            return VCARD_TYPE_DEFAULT;
        }
    }
    public static boolean isV30(final int vcardType) {
        return ((vcardType & FLAG_V30) != 0);  
    }
    public static boolean shouldUseQuotedPrintable(final int vcardType) {
        return !isV30(vcardType);
    }
    public static boolean usesUtf8(final int vcardType) {
        return ((vcardType & FLAG_CHARSET_MASK) == FLAG_CHARSET_UTF8);
    }
    public static boolean usesShiftJis(final int vcardType) {
        return ((vcardType & FLAG_CHARSET_MASK) == FLAG_CHARSET_SHIFT_JIS);
    }
    public static int getNameOrderType(final int vcardType) {
        return vcardType & NAME_ORDER_MASK;
    }
    public static boolean usesAndroidSpecificProperty(final int vcardType) {
        return ((vcardType & FLAG_USE_ANDROID_PROPERTY) != 0);
    }
    public static boolean usesDefactProperty(final int vcardType) {
        return ((vcardType & FLAG_USE_DEFACT_PROPERTY) != 0);
    }
    public static boolean showPerformanceLog() {
        return (VCardConfig.LOG_LEVEL & VCardConfig.LOG_LEVEL_PERFORMANCE_MEASUREMENT) != 0;
    }
    public static boolean shouldRefrainQPToNameProperties(final int vcardType) {
       return (!shouldUseQuotedPrintable(vcardType) ||
               ((vcardType & FLAG_REFRAIN_QP_TO_NAME_PROPERTIES) != 0));
    }
    public static boolean appendTypeParamName(final int vcardType) {
        return (isV30(vcardType) || ((vcardType & FLAG_APPEND_TYPE_PARAM) != 0));
    }
    public static boolean isJapaneseDevice(final int vcardType) {
        return sJapaneseMobileTypeSet.contains(vcardType);
    }
    public static boolean needsToConvertPhoneticString(final int vcardType) {
        return ((vcardType & FLAG_CONVERT_PHONETIC_NAME_STRINGS) != 0);
    }
    public static boolean onlyOneNoteFieldIsAvailable(final int vcardType) {
        return vcardType == VCARD_TYPE_DOCOMO;
    }
    public static boolean isDoCoMo(final int vcardType) {
        return ((vcardType & FLAG_DOCOMO) != 0);
    }
    private VCardConfig() {
    }
}
public class ImpsUtils {
    private static final HashMap<String, String> sClientInfo;
    private static String sSessionCookie;
    private static int sSessionCookieNumber;
    private ImpsUtils() {
    }
    static {
        sClientInfo = new HashMap<String, String>();
        sClientInfo.put(ImpsTags.ClientType, ImpsClientCapability.getClientType());
        sClientInfo.put(ImpsTags.ClientProducer, ImpsConstants.CLIENT_PRODUCER);
        sClientInfo.put(ImpsTags.ClientVersion, ImpsConstants.CLIENT_VERSION);
    }
    public static boolean isTrue(String value) {
        return ImpsConstants.TRUE.equalsIgnoreCase(value);
    }
    public static boolean isFalse(String value) {
        return ImpsConstants.FALSE.equalsIgnoreCase(value);
    }
    public static String toImpsBool(boolean isTrue) {
        if (isTrue) {
            return ImpsConstants.TRUE;
        }
        return ImpsConstants.FALSE;
    }
    public static ImpsErrorInfo checkResultError(Primitive response) {
        PrimitiveElement result = response.getElement(ImpsTags.Result);
        if (result == null) {
            return null;
        }
        String resultCode = result.getChild(ImpsTags.Code).getContents();
        if (!ImpsConstants.SUCCESS_CODE.equals(resultCode)) {
            PrimitiveElement descElem = result.getChild(ImpsTags.Description);
            String errorDesc = (descElem == null) ? "" : descElem.getContents();
            int statusCode = parseInt(resultCode, ImErrorInfo.ILLEGAL_SERVER_RESPONSE);
            return new ImpsErrorInfo(statusCode, errorDesc, response);
        }
        return null;
    }
    public static String trim(String str) {
        if (null == str || "".equals(str))
            return str;
        int strLen = str.length();
        int start = 0;
        while (start < strLen && Character.isWhitespace(str.charAt(start)))
            start++;
        int end = strLen - 1;
        while (end >= 0 && Character.isWhitespace(str.charAt(end)))
            end--;
        if (end < start)
            return "";
        str = str.substring(start, end + 1);
        return str;
    }
    public static boolean isQualifiedPresence(PrimitiveElement elem) {
        if (null == elem || null == elem.getChild(ImpsTags.Qualifier)) {
            return false;
        }
        return ImpsUtils.isTrue(elem.getChildContents(ImpsTags.Qualifier));
    }
    public static Map<String, String> getClientInfo() {
        return Collections.unmodifiableMap(sClientInfo);
    }
    synchronized static String genSessionCookie() {
        if(sSessionCookie == null) {
            Random random = new Random();
            sSessionCookie = System.currentTimeMillis() + "" + random.nextInt();
        }
        return sSessionCookie + (sSessionCookieNumber++);
    }
    public static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    public static long parseLong(String s, long defaultValue) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

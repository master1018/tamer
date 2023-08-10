public final class Util {
    public static final String DATE_REGEXP = "^\\d{8}(T\\d{6}([\\+\\-]\\d{4}|Z))?$";
    public static final String URL_REGEXP = "^[a-zA-Z]+:.+$";
    public static final String URN_REGEXP = "^urn:[a-zA-Z0-9][a-zA-Z0-9-]{1,31}:([a-zA-Z0-9()+,.:=@;$_!*'-]|%[0-9A-Fa-f]{2})+$";
    public static boolean matches(String regexp, String data) {
        try {
            return new RE(regexp).isMatch(data);
        } catch (REException e) {
            throw new RuntimeException("Bad regular expression: " + e.getMessage());
        }
    }
    public static boolean isISODate(String s) {
        return matches(DATE_REGEXP, s);
    }
    public static boolean isISOLang(String s) {
        return matches("^[a-zA-Z]{2}(-[a-zA-Z]{2,3})?$", s);
    }
    public static boolean isDuidRef(String ref) {
        return (ref.startsWith("#"));
    }
    public static boolean isNewsMLUrn(String urn) {
        return (isUrn(urn) && matches("^urn:newsml:[^:]*:\\d{8}:[^:]*:\\d+[UNA]$", urn));
    }
    public static boolean isHttpUrl(String url) {
        return (isUrl(url) && matches("^[hH][tT][tT][pP]:.+", url));
    }
    public static boolean isUrl(String url) {
        return matches(URL_REGEXP, url);
    }
    public static boolean isUrn(String urn) {
        return matches(URN_REGEXP, urn);
    }
}

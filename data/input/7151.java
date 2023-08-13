public abstract class AbstractFileTypeDetector
    extends FileTypeDetector
{
    protected AbstractFileTypeDetector() {
        super();
    }
    @Override
    public final String probeContentType(Path file) throws IOException {
        if (file == null)
            throw new NullPointerException("'file' is null");
        String result = implProbeContentType(file);
        return (result == null) ? null : parse(result);
    }
    protected abstract String implProbeContentType(Path file)
        throws IOException;
    private static String parse(String s) {
        int slash = s.indexOf('/');
        int semicolon = s.indexOf(';');
        if (slash < 0)
            return null;  
        String type = s.substring(0, slash).trim().toLowerCase(Locale.ENGLISH);
        if (!isValidToken(type))
            return null;  
        String subtype = (semicolon < 0) ? s.substring(slash + 1) :
            s.substring(slash + 1, semicolon);
        subtype = subtype.trim().toLowerCase(Locale.ENGLISH);
        if (!isValidToken(subtype))
            return null;  
        StringBuilder sb = new StringBuilder(type.length() + subtype.length() + 1);
        sb.append(type);
        sb.append('/');
        sb.append(subtype);
        return sb.toString();
    }
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";
    private static boolean isTokenChar(char c) {
        return (c > 040) && (c < 0177) && (TSPECIALS.indexOf(c) < 0);
    }
    private static boolean isValidToken(String s) {
        int len = s.length();
        if (len == 0)
            return false;
        for (int i = 0; i < len; i++) {
            if (!isTokenChar(s.charAt(i)))
                return false;
        }
        return true;
    }
}

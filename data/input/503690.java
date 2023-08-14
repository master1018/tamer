public class MailTo {
    static public final String MAILTO_SCHEME = "mailto:";
    private HashMap<String, String> mHeaders;
    static private final String TO = "to";
    static private final String BODY = "body";
    static private final String CC = "cc";
    static private final String SUBJECT = "subject";
    public static boolean isMailTo(String url) {
        if (url != null && url.startsWith(MAILTO_SCHEME)) {
            return true;
        }
        return false;
    }
    public static MailTo parse(String url) throws ParseException {
        if (url == null) {
            throw new NullPointerException();
        }
        if (!isMailTo(url)) {
             throw new ParseException("Not a mailto scheme");
        }
        String noScheme = url.substring(MAILTO_SCHEME.length());
        Uri email = Uri.parse(noScheme);
        MailTo m = new MailTo();
        String query = email.getQuery();
        if (query != null ) {
            String[] queries = query.split("&");
            for (String q : queries) {
                String[] nameval = q.split("=");
                if (nameval.length == 0) {
                    continue;
                }
                m.mHeaders.put(Uri.decode(nameval[0]).toLowerCase(), 
                        nameval.length > 1 ? Uri.decode(nameval[1]) : null);
            }
        }
        String address = email.getPath();
        if (address != null) {
            String addr = m.getTo();
            if (addr != null) {
                address += ", " + addr;
            }
            m.mHeaders.put(TO, address);
        }
        return m;
    }
    public String getTo() {
        return mHeaders.get(TO);
    }
    public String getCc() {
        return mHeaders.get(CC);
    }
    public String getSubject() {
        return mHeaders.get(SUBJECT);
    }
    public String getBody() {
        return mHeaders.get(BODY);
    }
    public Map<String, String> getHeaders() {
        return mHeaders;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(MAILTO_SCHEME);
        sb.append('?');
        for (Map.Entry<String,String> header : mHeaders.entrySet()) {
            sb.append(Uri.encode(header.getKey()));
            sb.append('=');
            sb.append(Uri.encode(header.getValue()));
            sb.append('&');
        }
        return sb.toString();
    }
    private MailTo() {
        mHeaders = new HashMap<String, String>();
    }
}

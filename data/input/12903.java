public class URLCanonicalizer {
    public URLCanonicalizer() { }
    public String canonicalize(String simpleURL) {
        String resultURL = simpleURL;
        if (simpleURL.startsWith("ftp.")) {
            resultURL = "ftp:
        } else if (simpleURL.startsWith("gopher.")) {
            resultURL = "gopher:
        } else if (simpleURL.startsWith("/")) {
            resultURL = "file:" + simpleURL;
        } else if (!hasProtocolName(simpleURL)) {
            if (isSimpleHostName(simpleURL)) {
                simpleURL = "www." + simpleURL + ".com";
            }
            resultURL = "http:
        }
        return resultURL;
    }
    public boolean hasProtocolName(String url) {
        int index = url.indexOf(':');
        if (index <= 0) {       
            return false;
        }
        for (int i = 0; i < index; i++) {
            char c = url.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c == '-')) {
                continue;
            }
            return false;
        }
        return true;
    }
    protected boolean isSimpleHostName(String url) {
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || (c == '-')) {
                continue;
            }
            return false;
        }
        return true;
    }
}

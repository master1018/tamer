public class AuthenticationHeader {
    MessageHeader rsp; 
    HeaderParser preferred;
    String preferred_r; 
    private final HttpCallerInfo hci;   
    boolean dontUseNegotiate = false;
    static String authPref=null;
    public String toString() {
        return "AuthenticationHeader: prefer " + preferred_r;
    }
    static {
        authPref = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("http.auth.preference"));
        if (authPref != null) {
            authPref = authPref.toLowerCase();
            if(authPref.equals("spnego") || authPref.equals("kerberos")) {
                authPref = "negotiate";
            }
        }
    }
    String hdrname; 
    public AuthenticationHeader (String hdrname, MessageHeader response,
            HttpCallerInfo hci, boolean dontUseNegotiate) {
        this.hci = hci;
        this.dontUseNegotiate = dontUseNegotiate;
        rsp = response;
        this.hdrname = hdrname;
        schemes = new HashMap();
        parse();
    }
    public HttpCallerInfo getHttpCallerInfo() {
        return hci;
    }
    static class SchemeMapValue {
        SchemeMapValue (HeaderParser h, String r) {raw=r; parser=h;}
        String raw;
        HeaderParser parser;
    }
    HashMap schemes;
    private void parse () {
        Iterator iter = rsp.multiValueIterator (hdrname);
        while (iter.hasNext()) {
            String raw = (String)iter.next();
            HeaderParser hp = new HeaderParser (raw);
            Iterator keys = hp.keys();
            int i, lastSchemeIndex;
            for (i=0, lastSchemeIndex = -1; keys.hasNext(); i++) {
                keys.next();
                if (hp.findValue(i) == null) { 
                    if (lastSchemeIndex != -1) {
                        HeaderParser hpn = hp.subsequence (lastSchemeIndex, i);
                        String scheme = hpn.findKey(0);
                        schemes.put (scheme, new SchemeMapValue (hpn, raw));
                    }
                    lastSchemeIndex = i;
                }
            }
            if (i > lastSchemeIndex) {
                HeaderParser hpn = hp.subsequence (lastSchemeIndex, i);
                String scheme = hpn.findKey(0);
                schemes.put (scheme, new SchemeMapValue (hpn, raw));
            }
        }
        SchemeMapValue v = null;
        if (authPref == null || (v=(SchemeMapValue)schemes.get (authPref)) == null) {
            if(v == null && !dontUseNegotiate) {
                SchemeMapValue tmp = (SchemeMapValue)schemes.get("negotiate");
                if(tmp != null) {
                    if(hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(hci, "Negotiate"))) {
                        tmp = null;
                    }
                    v = tmp;
                }
            }
            if(v == null && !dontUseNegotiate) {
                SchemeMapValue tmp = (SchemeMapValue)schemes.get("kerberos");
                if(tmp != null) {
                    if(hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(hci, "Kerberos"))) {
                        tmp = null;
                    }
                    v = tmp;
                }
            }
            if(v == null) {
                if ((v=(SchemeMapValue)schemes.get ("digest")) == null) {
                    if (((v=(SchemeMapValue)schemes.get("ntlm"))==null)) {
                        v = (SchemeMapValue)schemes.get ("basic");
                    }
                }
            }
        } else {    
            if (dontUseNegotiate && authPref.equals("negotiate")) {
                v = null;
            }
        }
        if (v != null) {
            preferred = v.parser;
            preferred_r = v.raw;
        }
    }
    public HeaderParser headerParser() {
        return preferred;
    }
    public String scheme() {
        if (preferred != null) {
            return preferred.findKey(0);
        } else {
            return null;
        }
    }
    public String raw () {
        return preferred_r;
    }
    public boolean isPresent () {
        return preferred != null;
    }
}

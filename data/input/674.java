class NegotiateAuthentication extends AuthenticationInfo {
    private static final long serialVersionUID = 100L;
    final private HttpCallerInfo hci;
    static HashMap <String, Boolean> supported = null;
    static HashMap <String, Negotiator> cache = null;
    private Negotiator negotiator = null;
    public NegotiateAuthentication(HttpCallerInfo hci) {
        super(RequestorType.PROXY==hci.authType ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
              hci.scheme.equalsIgnoreCase("Negotiate") ? NEGOTIATE : KERBEROS,
              hci.url,
              "");
        this.hci = hci;
    }
    @Override
    public boolean supportsPreemptiveAuthorization() {
        return false;
    }
    synchronized public static boolean isSupported(HttpCallerInfo hci) {
        if (supported == null) {
            supported = new HashMap <String, Boolean>();
            cache = new HashMap <String, Negotiator>();
        }
        String hostname = hci.host;
        hostname = hostname.toLowerCase();
        if (supported.containsKey(hostname)) {
            return supported.get(hostname);
        }
        Negotiator neg = Negotiator.getNegotiator(hci);
        if (neg != null) {
            supported.put(hostname, true);
            cache.put(hostname, neg);
            return true;
        } else {
            supported.put(hostname, false);
            return false;
        }
    }
    @Override
    public String getHeaderValue(URL url, String method) {
        throw new RuntimeException ("getHeaderValue not supported");
    }
    @Override
    public boolean isAuthorizationStale (String header) {
        return false; 
    }
    @Override
    public synchronized boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        try {
            String response;
            byte[] incoming = null;
            String[] parts = raw.split("\\s+");
            if (parts.length > 1) {
                incoming = new BASE64Decoder().decodeBuffer(parts[1]);
            }
            response = hci.scheme + " " + new B64Encoder().encode(
                        incoming==null?firstToken():nextToken(incoming));
            conn.setAuthenticationProperty(getHeaderName(), response);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private byte[] firstToken() throws IOException {
        negotiator = null;
        if (cache != null) {
            synchronized(cache) {
                negotiator = cache.get(getHost());
                if (negotiator != null) {
                    cache.remove(getHost()); 
                }
            }
        }
        if (negotiator == null) {
            negotiator = Negotiator.getNegotiator(hci);
            if (negotiator == null) {
                IOException ioe = new IOException("Cannot initialize Negotiator");
                throw ioe;
            }
        }
        return negotiator.firstToken();
    }
    private byte[] nextToken(byte[] token) throws IOException {
        return negotiator.nextToken(token);
    }
    class B64Encoder extends BASE64Encoder {
        protected int bytesPerLine () {
            return 100000;  
        }
    }
}

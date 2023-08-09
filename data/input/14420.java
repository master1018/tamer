public final class KdcComm {
    private static int defaultKdcRetryLimit;
    private static int defaultKdcTimeout;
    private static int defaultUdpPrefLimit;
    private static final boolean DEBUG = Krb5.DEBUG;
    private static final String BAD_POLICY_KEY = "krb5.kdc.bad.policy";
    private enum BpType {
        NONE, TRY_LAST, TRY_LESS
    }
    private static int tryLessMaxRetries = 1;
    private static int tryLessTimeout = 5000;
    private static BpType badPolicy;
    static {
        initStatic();
    }
    public static void initStatic() {
        String value = AccessController.doPrivileged(
        new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(BAD_POLICY_KEY);
            }
        });
        if (value != null) {
            value = value.toLowerCase(Locale.ENGLISH);
            String[] ss = value.split(":");
            if ("tryless".equals(ss[0])) {
                if (ss.length > 1) {
                    String[] params = ss[1].split(",");
                    try {
                        int tmp0 = Integer.parseInt(params[0]);
                        if (params.length > 1) {
                            tryLessTimeout = Integer.parseInt(params[1]);
                        }
                        tryLessMaxRetries = tmp0;
                    } catch (NumberFormatException nfe) {
                        if (DEBUG) {
                            System.out.println("Invalid " + BAD_POLICY_KEY +
                                    " parameter for tryLess: " +
                                    value + ", use default");
                        }
                    }
                }
                badPolicy = BpType.TRY_LESS;
            } else if ("trylast".equals(ss[0])) {
                badPolicy = BpType.TRY_LAST;
            } else {
                badPolicy = BpType.NONE;
            }
        } else {
            badPolicy = BpType.NONE;
        }
        int timeout = -1;
        int max_retries = -1;
        int udf_pref_limit = -1;
        try {
            Config cfg = Config.getInstance();
            String temp = cfg.getDefault("kdc_timeout", "libdefaults");
            timeout = parsePositiveIntString(temp);
            temp = cfg.getDefault("max_retries", "libdefaults");
            max_retries = parsePositiveIntString(temp);
            temp = cfg.getDefault("udp_preference_limit", "libdefaults");
            udf_pref_limit = parsePositiveIntString(temp);
        } catch (Exception exc) {
           if (DEBUG) {
                System.out.println ("Exception in getting KDC communication " +
                                    "settings, using default value " +
                                    exc.getMessage());
           }
        }
        defaultKdcTimeout = timeout > 0 ? timeout : 30*1000; 
        defaultKdcRetryLimit =
                max_retries > 0 ? max_retries : Krb5.KDC_RETRY_LIMIT;
        defaultUdpPrefLimit = udf_pref_limit;
        KdcAccessibility.reset();
    }
    private String realm;
    public KdcComm(String realm) throws KrbException {
        if (realm == null) {
           realm = Config.getInstance().getDefaultRealm();
            if (realm == null) {
                throw new KrbException(Krb5.KRB_ERR_GENERIC,
                                       "Cannot find default realm");
            }
        }
        this.realm = realm;
    }
    public byte[] send(byte[] obuf)
        throws IOException, KrbException {
        int udpPrefLimit = getRealmSpecificValue(
                realm, "udp_preference_limit", defaultUdpPrefLimit);
        boolean useTCP = (udpPrefLimit > 0 &&
             (obuf != null && obuf.length > udpPrefLimit));
        return send(obuf, useTCP);
    }
    private byte[] send(byte[] obuf, boolean useTCP)
        throws IOException, KrbException {
        if (obuf == null)
            return null;
        Exception savedException = null;
        Config cfg = Config.getInstance();
        if (realm == null) {
            realm = cfg.getDefaultRealm();
            if (realm == null) {
                throw new KrbException(Krb5.KRB_ERR_GENERIC,
                                       "Cannot find default realm");
            }
        }
        String kdcList = cfg.getKDCList(realm);
        if (kdcList == null) {
            throw new KrbException("Cannot get kdc for realm " + realm);
        }
        String tempKdc = null; 
        byte[] ibuf = null;
        for (String tmp: KdcAccessibility.list(kdcList)) {
            tempKdc = tmp;
            try {
                ibuf = send(obuf,tempKdc,useTCP);
                KRBError ke = null;
                try {
                    ke = new KRBError(ibuf);
                } catch (Exception e) {
                }
                if (ke != null && ke.getErrorCode() ==
                        Krb5.KRB_ERR_RESPONSE_TOO_BIG) {
                    ibuf = send(obuf, tempKdc, true);
                }
                KdcAccessibility.removeBad(tempKdc);
                break;
            } catch (Exception e) {
                if (DEBUG) {
                    System.out.println(">>> KrbKdcReq send: error trying " +
                            tempKdc);
                    e.printStackTrace(System.out);
                }
                KdcAccessibility.addBad(tempKdc);
                savedException = e;
            }
        }
        if (ibuf == null && savedException != null) {
            if (savedException instanceof IOException) {
                throw (IOException) savedException;
            } else {
                throw (KrbException) savedException;
            }
        }
        return ibuf;
    }
    private byte[] send(byte[] obuf, String tempKdc, boolean useTCP)
        throws IOException, KrbException {
        if (obuf == null)
            return null;
        int port = Krb5.KDC_INET_DEFAULT_PORT;
        int retries = getRealmSpecificValue(
                realm, "max_retries", defaultKdcRetryLimit);
        int timeout = getRealmSpecificValue(
                realm, "kdc_timeout", defaultKdcTimeout);
        if (badPolicy == BpType.TRY_LESS &&
                KdcAccessibility.isBad(tempKdc)) {
            if (retries > tryLessMaxRetries) {
                retries = tryLessMaxRetries; 
            }
            if (timeout > tryLessTimeout) {
                timeout = tryLessTimeout; 
            }
        }
        String kdc = null;
        String portStr = null;
        if (tempKdc.charAt(0) == '[') {     
            int pos = tempKdc.indexOf(']', 1);
            if (pos == -1) {
                throw new IOException("Illegal KDC: " + tempKdc);
            }
            kdc = tempKdc.substring(1, pos);
            if (pos != tempKdc.length() - 1) {  
                if (tempKdc.charAt(pos+1) != ':') {
                    throw new IOException("Illegal KDC: " + tempKdc);
                }
                portStr = tempKdc.substring(pos+2);
            }
        } else {
            int colon = tempKdc.indexOf(':');
            if (colon == -1) {      
                kdc = tempKdc;
            } else {
                int nextColon = tempKdc.indexOf(':', colon+1);
                if (nextColon > 0) {    
                    kdc = tempKdc;
                } else {                
                    kdc = tempKdc.substring(0, colon);
                    portStr = tempKdc.substring(colon+1);
                }
            }
        }
        if (portStr != null) {
            int tempPort = parsePositiveIntString(portStr);
            if (tempPort > 0)
                port = tempPort;
        }
        if (DEBUG) {
            System.out.println(">>> KrbKdcReq send: kdc=" + kdc
                               + (useTCP ? " TCP:":" UDP:")
                               +  port +  ", timeout="
                               + timeout
                               + ", number of retries ="
                               + retries
                               + ", #bytes=" + obuf.length);
        }
        KdcCommunication kdcCommunication =
            new KdcCommunication(kdc, port, useTCP, timeout, retries, obuf);
        try {
            byte[] ibuf = AccessController.doPrivileged(kdcCommunication);
            if (DEBUG) {
                System.out.println(">>> KrbKdcReq send: #bytes read="
                        + (ibuf != null ? ibuf.length : 0));
            }
            return ibuf;
        } catch (PrivilegedActionException e) {
            Exception wrappedException = e.getException();
            if (wrappedException instanceof IOException) {
                throw (IOException) wrappedException;
            } else {
                throw (KrbException) wrappedException;
            }
        }
    }
    private static class KdcCommunication
        implements PrivilegedExceptionAction<byte[]> {
        private String kdc;
        private int port;
        private boolean useTCP;
        private int timeout;
        private int retries;
        private byte[] obuf;
        public KdcCommunication(String kdc, int port, boolean useTCP,
                                int timeout, int retries, byte[] obuf) {
            this.kdc = kdc;
            this.port = port;
            this.useTCP = useTCP;
            this.timeout = timeout;
            this.retries = retries;
            this.obuf = obuf;
        }
        public byte[] run() throws IOException, KrbException {
            byte[] ibuf = null;
            for (int i=1; i <= retries; i++) {
                String proto = useTCP?"TCP":"UDP";
                NetClient kdcClient = NetClient.getInstance(
                        proto, kdc, port, timeout);
                if (DEBUG) {
                    System.out.println(">>> KDCCommunication: kdc=" + kdc
                           + " " + proto + ":"
                           +  port +  ", timeout="
                           + timeout
                           + ",Attempt =" + i
                           + ", #bytes=" + obuf.length);
                }
                try {
                    kdcClient.send(obuf);
                    ibuf = kdcClient.receive();
                    break;
                } catch (SocketTimeoutException se) {
                    if (DEBUG) {
                        System.out.println ("SocketTimeOutException with " +
                                            "attempt: " + i);
                    }
                    if (i == retries) {
                        ibuf = null;
                        throw se;
                    }
                } finally {
                    kdcClient.close();
                }
            }
            return ibuf;
        }
    }
    private int getRealmSpecificValue(String realm, String key, int defValue) {
        int v = defValue;
        if (realm == null) return v;
        int temp = -1;
        try {
            String value =
               Config.getInstance().getDefault(key, realm);
            temp = parsePositiveIntString(value);
        } catch (Exception exc) {
        }
        if (temp > 0) v = temp;
        return v;
    }
    private static int parsePositiveIntString(String intString) {
        if (intString == null)
            return -1;
        int ret = -1;
        try {
            ret = Integer.parseInt(intString);
        } catch (Exception exc) {
            return -1;
        }
        if (ret >= 0)
            return ret;
        return -1;
    }
    static class KdcAccessibility {
        private static Set<String> bads = new HashSet<>();
        private static synchronized void addBad(String kdc) {
            if (DEBUG) {
                System.out.println(">>> KdcAccessibility: add " + kdc);
            }
            bads.add(kdc);
        }
        private static synchronized void removeBad(String kdc) {
            if (DEBUG) {
                System.out.println(">>> KdcAccessibility: remove " + kdc);
            }
            bads.remove(kdc);
        }
        private static synchronized boolean isBad(String kdc) {
            return bads.contains(kdc);
        }
        private static synchronized void reset() {
            if (DEBUG) {
                System.out.println(">>> KdcAccessibility: reset");
            }
            bads.clear();
        }
        private static synchronized String[] list(String kdcList) {
            StringTokenizer st = new StringTokenizer(kdcList);
            List<String> list = new ArrayList<>();
            if (badPolicy == BpType.TRY_LAST) {
                List<String> badkdcs = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    String t = st.nextToken();
                    if (bads.contains(t)) badkdcs.add(t);
                    else list.add(t);
                }
                list.addAll(badkdcs);
            } else {
                while (st.hasMoreTokens()) {
                    list.add(st.nextToken());
                }
            }
            return list.toArray(new String[list.size()]);
        }
    }
}

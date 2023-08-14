public class KerberosTicket implements Destroyable, Refreshable,
         java.io.Serializable {
    private static final long serialVersionUID = 7395334370157380539L;
    private static final int FORWARDABLE_TICKET_FLAG = 1;
    private static final int FORWARDED_TICKET_FLAG   = 2;
    private static final int PROXIABLE_TICKET_FLAG   = 3;
    private static final int PROXY_TICKET_FLAG       = 4;
    private static final int POSTDATED_TICKET_FLAG   = 6;
    private static final int RENEWABLE_TICKET_FLAG   = 8;
    private static final int INITIAL_TICKET_FLAG     = 9;
    private static final int NUM_FLAGS = 32;
    private byte[] asn1Encoding;
    private KeyImpl sessionKey;
    private boolean[] flags;
    private Date authTime;
    private Date startTime;
    private Date endTime;
    private Date renewTill;
    private KerberosPrincipal client;
    private KerberosPrincipal server;
    private InetAddress[] clientAddresses;
    private transient boolean destroyed = false;
    public KerberosTicket(byte[] asn1Encoding,
                         KerberosPrincipal client,
                         KerberosPrincipal server,
                         byte[] sessionKey,
                         int keyType,
                         boolean[] flags,
                         Date authTime,
                         Date startTime,
                         Date endTime,
                         Date renewTill,
                         InetAddress[] clientAddresses) {
        init(asn1Encoding, client, server, sessionKey, keyType, flags,
            authTime, startTime, endTime, renewTill, clientAddresses);
    }
    private void init(byte[] asn1Encoding,
                         KerberosPrincipal client,
                         KerberosPrincipal server,
                         byte[] sessionKey,
                         int keyType,
                         boolean[] flags,
                         Date authTime,
                         Date startTime,
                         Date endTime,
                         Date renewTill,
                         InetAddress[] clientAddresses) {
        if (sessionKey == null)
           throw new IllegalArgumentException("Session key for ticket"
                                              + " cannot be null");
        init(asn1Encoding, client, server,
             new KeyImpl(sessionKey, keyType), flags, authTime,
             startTime, endTime, renewTill, clientAddresses);
    }
    private void init(byte[] asn1Encoding,
                         KerberosPrincipal client,
                         KerberosPrincipal server,
                         KeyImpl sessionKey,
                         boolean[] flags,
                         Date authTime,
                         Date startTime,
                         Date endTime,
                         Date renewTill,
                         InetAddress[] clientAddresses) {
        if (asn1Encoding == null)
           throw new IllegalArgumentException("ASN.1 encoding of ticket"
                                              + " cannot be null");
        this.asn1Encoding = asn1Encoding.clone();
        if (client == null)
           throw new IllegalArgumentException("Client name in ticket"
                                              + " cannot be null");
        this.client = client;
        if (server == null)
           throw new IllegalArgumentException("Server name in ticket"
                                              + " cannot be null");
        this.server = server;
        this.sessionKey = sessionKey;
        if (flags != null) {
           if (flags.length >= NUM_FLAGS)
                this.flags = flags.clone();
           else {
                this.flags = new boolean[NUM_FLAGS];
                for (int i = 0; i < flags.length; i++)
                    this.flags[i] = flags[i];
           }
        } else
           this.flags = new boolean[NUM_FLAGS];
        if (this.flags[RENEWABLE_TICKET_FLAG]) {
           if (renewTill == null)
                throw new IllegalArgumentException("The renewable period "
                       + "end time cannot be null for renewable tickets.");
           this.renewTill = new Date(renewTill.getTime());
        }
        if (authTime != null) {
            this.authTime = new Date(authTime.getTime());
        }
        if (startTime != null) {
            this.startTime = new Date(startTime.getTime());
        } else {
            this.startTime = this.authTime;
        }
        if (endTime == null)
           throw new IllegalArgumentException("End time for ticket validity"
                                              + " cannot be null");
        this.endTime = new Date(endTime.getTime());
        if (clientAddresses != null)
           this.clientAddresses = clientAddresses.clone();
    }
    public final KerberosPrincipal getClient() {
        return client;
    }
    public final KerberosPrincipal getServer() {
        return server;
    }
    public final SecretKey getSessionKey() {
        if (destroyed)
            throw new IllegalStateException("This ticket is no longer valid");
        return sessionKey;
    }
    public final int getSessionKeyType() {
        if (destroyed)
            throw new IllegalStateException("This ticket is no longer valid");
        return sessionKey.getKeyType();
    }
    public final boolean isForwardable() {
        return flags[FORWARDABLE_TICKET_FLAG];
    }
    public final boolean isForwarded() {
        return flags[FORWARDED_TICKET_FLAG];
    }
    public final boolean isProxiable() {
        return flags[PROXIABLE_TICKET_FLAG];
    }
    public final boolean isProxy() {
        return flags[PROXY_TICKET_FLAG];
    }
    public final boolean isPostdated() {
        return flags[POSTDATED_TICKET_FLAG];
    }
    public final boolean isRenewable() {
        return flags[RENEWABLE_TICKET_FLAG];
    }
    public final boolean isInitial() {
        return flags[INITIAL_TICKET_FLAG];
    }
    public final boolean[]  getFlags() {
        return (flags == null? null: flags.clone());
    }
    public final java.util.Date getAuthTime() {
        return (authTime == null) ? null : (Date)authTime.clone();
    }
    public final java.util.Date getStartTime() {
        return (startTime == null) ? null : (Date)startTime.clone();
    }
    public final java.util.Date getEndTime() {
        return (Date) endTime.clone();
    }
    public final java.util.Date getRenewTill() {
        return (renewTill == null) ? null: (Date)renewTill.clone();
    }
    public final java.net.InetAddress[] getClientAddresses() {
        return (clientAddresses == null) ? null: clientAddresses.clone();
    }
    public final byte[] getEncoded() {
        if (destroyed)
            throw new IllegalStateException("This ticket is no longer valid");
        return asn1Encoding.clone();
    }
    public boolean isCurrent() {
        return (System.currentTimeMillis() <= getEndTime().getTime());
    }
    public void refresh() throws RefreshFailedException {
        if (destroyed)
            throw new RefreshFailedException("A destroyed ticket "
                                             + "cannot be renewd.");
        if (!isRenewable())
            throw new RefreshFailedException("This ticket is not renewable");
        if (System.currentTimeMillis() > getRenewTill().getTime())
            throw new RefreshFailedException("This ticket is past "
                                             + "its last renewal time.");
        Throwable e = null;
        sun.security.krb5.Credentials krb5Creds = null;
        try {
            krb5Creds = new sun.security.krb5.Credentials(asn1Encoding,
                                                    client.toString(),
                                                    server.toString(),
                                                    sessionKey.getEncoded(),
                                                    sessionKey.getKeyType(),
                                                    flags,
                                                    authTime,
                                                    startTime,
                                                    endTime,
                                                    renewTill,
                                                    clientAddresses);
            krb5Creds = krb5Creds.renew();
        } catch (sun.security.krb5.KrbException krbException) {
            e = krbException;
        } catch (java.io.IOException ioException) {
            e = ioException;
        }
        if (e != null) {
            RefreshFailedException rfException
                = new RefreshFailedException("Failed to renew Kerberos Ticket "
                                             + "for client " + client
                                             + " and server " + server
                                             + " - " + e.getMessage());
            rfException.initCause(e);
            throw rfException;
        }
        synchronized (this) {
            try {
                this.destroy();
            } catch (DestroyFailedException dfException) {
            }
            init(krb5Creds.getEncoded(),
                 new KerberosPrincipal(krb5Creds.getClient().getName()),
                 new KerberosPrincipal(krb5Creds.getServer().getName(),
                                        KerberosPrincipal.KRB_NT_SRV_INST),
                 krb5Creds.getSessionKey().getBytes(),
                 krb5Creds.getSessionKey().getEType(),
                 krb5Creds.getFlags(),
                 krb5Creds.getAuthTime(),
                 krb5Creds.getStartTime(),
                 krb5Creds.getEndTime(),
                 krb5Creds.getRenewTill(),
                 krb5Creds.getClientAddresses());
            destroyed = false;
        }
    }
    public void destroy() throws DestroyFailedException {
        if (!destroyed) {
            Arrays.fill(asn1Encoding, (byte) 0);
            client = null;
            server = null;
            sessionKey.destroy();
            flags = null;
            authTime = null;
            startTime = null;
            endTime = null;
            renewTill = null;
            clientAddresses = null;
            destroyed = true;
        }
    }
    public boolean isDestroyed() {
        return destroyed;
    }
    public String toString() {
        if (destroyed)
            throw new IllegalStateException("This ticket is no longer valid");
        StringBuffer caddrBuf = new StringBuffer();
        if (clientAddresses != null) {
            for (int i = 0; i < clientAddresses.length; i++) {
                caddrBuf.append("clientAddresses[" + i + "] = " +
                                 clientAddresses[i].toString());
            }
        }
        return ("Ticket (hex) = " + "\n" +
                 (new HexDumpEncoder()).encodeBuffer(asn1Encoding) + "\n" +
                "Client Principal = " + client.toString() + "\n" +
                "Server Principal = " + server.toString() + "\n" +
                "Session Key = " + sessionKey.toString() + "\n" +
                "Forwardable Ticket " + flags[FORWARDABLE_TICKET_FLAG] + "\n" +
                "Forwarded Ticket " + flags[FORWARDED_TICKET_FLAG] + "\n" +
                "Proxiable Ticket " + flags[PROXIABLE_TICKET_FLAG] + "\n" +
                "Proxy Ticket " + flags[PROXY_TICKET_FLAG] + "\n" +
                "Postdated Ticket " + flags[POSTDATED_TICKET_FLAG] + "\n" +
                "Renewable Ticket " + flags[RENEWABLE_TICKET_FLAG] + "\n" +
                "Initial Ticket " + flags[RENEWABLE_TICKET_FLAG] + "\n" +
                "Auth Time = " + String.valueOf(authTime) + "\n" +
                "Start Time = " + String.valueOf(startTime) + "\n" +
                "End Time = " + endTime.toString() + "\n" +
                "Renew Till = " + String.valueOf(renewTill) + "\n" +
                "Client Addresses " +
                (clientAddresses == null ? " Null " : caddrBuf.toString() +
                "\n"));
    }
    public int hashCode() {
        int result = 17;
        if (isDestroyed()) {
            return result;
        }
        result = result * 37 + Arrays.hashCode(getEncoded());
        result = result * 37 + endTime.hashCode();
        result = result * 37 + client.hashCode();
        result = result * 37 + server.hashCode();
        result = result * 37 + sessionKey.hashCode();
        if (authTime != null) {
            result = result * 37 + authTime.hashCode();
        }
        if (startTime != null) {
            result = result * 37 + startTime.hashCode();
        }
        if (renewTill != null) {
            result = result * 37 + renewTill.hashCode();
        }
        result = result * 37 + Arrays.hashCode(clientAddresses);
        return result * 37 + Arrays.hashCode(flags);
    }
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (! (other instanceof KerberosTicket)) {
            return false;
        }
        KerberosTicket otherTicket = ((KerberosTicket) other);
        if (isDestroyed() || otherTicket.isDestroyed()) {
            return false;
        }
        if (!Arrays.equals(getEncoded(), otherTicket.getEncoded()) ||
                !endTime.equals(otherTicket.getEndTime()) ||
                !server.equals(otherTicket.getServer()) ||
                !client.equals(otherTicket.getClient()) ||
                !sessionKey.equals(otherTicket.getSessionKey()) ||
                !Arrays.equals(clientAddresses, otherTicket.getClientAddresses()) ||
                !Arrays.equals(flags, otherTicket.getFlags())) {
            return false;
        }
        if (authTime == null) {
            if (otherTicket.getAuthTime() != null)
                return false;
        } else {
            if (!authTime.equals(otherTicket.getAuthTime()))
                return false;
        }
        if (startTime == null) {
            if (otherTicket.getStartTime() != null)
                return false;
        } else {
            if (!startTime.equals(otherTicket.getStartTime()))
                return false;
        }
        if (renewTill == null) {
            if (otherTicket.getRenewTill() != null)
                return false;
        } else {
            if (!renewTill.equals(otherTicket.getRenewTill()))
                return false;
        }
        return true;
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (sessionKey == null) {
           throw new InvalidObjectException("Session key cannot be null");
        }
        try {
            init(asn1Encoding, client, server, sessionKey,
                 flags, authTime, startTime, endTime,
                 renewTill, clientAddresses);
        } catch (IllegalArgumentException iae) {
            throw (InvalidObjectException)
                new InvalidObjectException(iae.getMessage()).initCause(iae);
        }
    }
}

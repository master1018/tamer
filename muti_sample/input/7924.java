public final class KerberosClientKeyExchangeImpl
    extends sun.security.ssl.KerberosClientKeyExchange {
    private KerberosPreMasterSecret preMaster;
    private byte[] encodedTicket;
    private KerberosPrincipal peerPrincipal;
    private KerberosPrincipal localPrincipal;
    public KerberosClientKeyExchangeImpl() {
    }
    @Override
    public void init(String serverName, boolean isLoopback,
        AccessControlContext acc, ProtocolVersion protocolVersion,
        SecureRandom rand) throws IOException {
         KerberosTicket ticket = getServiceTicket(serverName, isLoopback, acc);
         encodedTicket = ticket.getEncoded();
         peerPrincipal = ticket.getServer();
         localPrincipal = ticket.getClient();
         EncryptionKey sessionKey = new EncryptionKey(
                                        ticket.getSessionKeyType(),
                                        ticket.getSessionKey().getEncoded());
         preMaster = new KerberosPreMasterSecret(protocolVersion,
             rand, sessionKey);
    }
    @Override
    public void init(ProtocolVersion protocolVersion,
        ProtocolVersion clientVersion,
        SecureRandom rand, HandshakeInStream input, SecretKey[] secretKeys)
        throws IOException {
        KerberosKey[] serverKeys = (KerberosKey[])secretKeys;
        encodedTicket = input.getBytes16();
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(System.out,
                "encoded Kerberos service ticket", encodedTicket);
        }
        EncryptionKey sessionKey = null;
        try {
            Ticket t = new Ticket(encodedTicket);
            EncryptedData encPart = t.encPart;
            PrincipalName ticketSname = t.sname;
            Realm ticketRealm = t.realm;
            String serverPrincipal = serverKeys[0].getPrincipal().getName();
            String ticketPrinc = ticketSname.toString().concat("@" +
                                        ticketRealm.toString());
            if (!ticketPrinc.equals(serverPrincipal)) {
                if (debug != null && Debug.isOn("handshake"))
                   System.out.println("Service principal in Ticket does not"
                        + " match associated principal in KerberosKey");
                throw new IOException("Server principal is " +
                    serverPrincipal + " but ticket is for " +
                    ticketPrinc);
            }
            int encPartKeyType = encPart.getEType();
            Integer encPartKeyVersion = encPart.getKeyVersionNumber();
            KerberosKey dkey = null;
            try {
                dkey = findKey(encPartKeyType, encPartKeyVersion, serverKeys);
            } catch (KrbException ke) { 
                throw new IOException(
                        "Cannot find key matching version number", ke);
            }
            if (dkey == null) {
                throw new IOException(
        "Cannot find key of appropriate type to decrypt ticket - need etype " +
                                   encPartKeyType);
            }
            EncryptionKey secretKey = new EncryptionKey(
                encPartKeyType,
                dkey.getEncoded());
            byte[] bytes = encPart.decrypt(secretKey, KeyUsage.KU_TICKET);
            byte[] temp = encPart.reset(bytes);
            EncTicketPart encTicketPart = new EncTicketPart(temp);
            peerPrincipal =
                new KerberosPrincipal(encTicketPart.cname.getName());
            localPrincipal = new KerberosPrincipal(ticketSname.getName());
            sessionKey = encTicketPart.key;
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("server principal: " + serverPrincipal);
                System.out.println("realm: " + encTicketPart.crealm.toString());
                System.out.println("cname: " + encTicketPart.cname.toString());
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("KerberosWrapper error getting session key,"
                        + " generating random secret (" + e.getMessage() + ")");
            }
            sessionKey = null;
        }
        input.getBytes16();   
        if (sessionKey != null) {
            preMaster = new KerberosPreMasterSecret(protocolVersion,
                clientVersion, rand, input, sessionKey);
        } else {
            preMaster = new KerberosPreMasterSecret(clientVersion, rand);
        }
    }
    @Override
    public int messageLength() {
        return (6 + encodedTicket.length + preMaster.getEncrypted().length);
    }
    @Override
    public void send(HandshakeOutStream s) throws IOException {
        s.putBytes16(encodedTicket);
        s.putBytes16(null); 
        s.putBytes16(preMaster.getEncrypted());
    }
    @Override
    public void print(PrintStream s) throws IOException {
        s.println("*** ClientKeyExchange, Kerberos");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "Kerberos service ticket", encodedTicket);
            Debug.println(s, "Random Secret", preMaster.getUnencrypted());
            Debug.println(s, "Encrypted random Secret",
                preMaster.getEncrypted());
        }
    }
    private static KerberosTicket getServiceTicket(String srvName,
        boolean isLoopback, final AccessControlContext acc) throws IOException {
        String serverName = srvName;
        if (isLoopback) {
            String localHost = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<String>() {
                public String run() {
                    String hostname;
                    try {
                        hostname = InetAddress.getLocalHost().getHostName();
                    } catch (java.net.UnknownHostException e) {
                        hostname = "localhost";
                    }
                    return hostname;
                }
            });
          serverName = localHost;
        }
        String serviceName = "host/" + serverName;
        PrincipalName principal;
        try {
            principal = new PrincipalName(serviceName,
                                PrincipalName.KRB_NT_SRV_HST);
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            IOException ioe = new IOException("Invalid service principal" +
                                " name: " + serviceName);
            ioe.initCause(e);
            throw ioe;
        }
        String realm = principal.getRealmAsString();
        final String serverPrincipal = principal.toString();
        final String tgsPrincipal = "krbtgt/" + realm + "@" + realm;
        final String clientPrincipal = null;  
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
           sm.checkPermission(new ServicePermission(serverPrincipal,
                                "initiate"), acc);
        }
        try {
            KerberosTicket ticket = AccessController.doPrivileged(
                new PrivilegedExceptionAction<KerberosTicket>() {
                public KerberosTicket run() throws Exception {
                    return Krb5Util.getTicketFromSubjectAndTgs(
                        GSSCaller.CALLER_SSL_CLIENT,
                        clientPrincipal, serverPrincipal,
                        tgsPrincipal, acc);
                        }});
            if (ticket == null) {
                throw new IOException("Failed to find any kerberos service" +
                        " ticket for " + serverPrincipal);
            }
            return ticket;
        } catch (PrivilegedActionException e) {
            IOException ioe = new IOException(
                "Attempt to obtain kerberos service ticket for " +
                        serverPrincipal + " failed!");
            ioe.initCause(e);
            throw ioe;
        }
    }
    @Override
    public byte[] getUnencryptedPreMasterSecret() {
        return preMaster.getUnencrypted();
    }
    @Override
    public KerberosPrincipal getPeerPrincipal() {
        return peerPrincipal;
    }
    @Override
    public KerberosPrincipal getLocalPrincipal() {
        return localPrincipal;
    }
    private static boolean versionMatches(Integer v1, int v2) {
        if (v1 == null || v1 == 0 || v2 == 0) {
            return true;
        }
        return v1.equals(v2);
    }
    private static KerberosKey findKey(int etype, Integer version,
            KerberosKey[] keys) throws KrbException {
        int ktype;
        boolean etypeFound = false;
        for (int i = 0; i < keys.length; i++) {
            ktype = keys[i].getKeyType();
            if (etype == ktype) {
                etypeFound = true;
                if (versionMatches(version, keys[i].getVersionNumber())) {
                    return keys[i];
                }
            }
        }
        if ((etype == EncryptedData.ETYPE_DES_CBC_CRC ||
            etype == EncryptedData.ETYPE_DES_CBC_MD5)) {
            for (int i = 0; i < keys.length; i++) {
                ktype = keys[i].getKeyType();
                if (ktype == EncryptedData.ETYPE_DES_CBC_CRC ||
                        ktype == EncryptedData.ETYPE_DES_CBC_MD5) {
                    etypeFound = true;
                    if (versionMatches(version, keys[i].getVersionNumber())) {
                        return new KerberosKey(keys[i].getPrincipal(),
                            keys[i].getEncoded(),
                            etype,
                            keys[i].getVersionNumber());
                    }
                }
            }
        }
        if (etypeFound) {
            throw new KrbException(Krb5.KRB_AP_ERR_BADKEYVER);
        }
        return null;
    }
}

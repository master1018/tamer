public class Krb5Util {
    static final boolean DEBUG =
        java.security.AccessController.doPrivileged(
            new sun.security.action.GetBooleanAction
            ("sun.security.krb5.debug")).booleanValue();
    private Krb5Util() {  
    }
    public static KerberosTicket getTicketFromSubjectAndTgs(GSSCaller caller,
        String clientPrincipal, String serverPrincipal, String tgsPrincipal,
        AccessControlContext acc)
        throws LoginException, KrbException, IOException {
        Subject accSubj = Subject.getSubject(acc);
        KerberosTicket ticket = SubjectComber.find(accSubj,
            serverPrincipal, clientPrincipal, KerberosTicket.class);
        if (ticket != null) {
            return ticket;  
        }
        Subject loginSubj = null;
        if (!GSSUtil.useSubjectCredsOnly(caller)) {
            try {
                loginSubj = GSSUtil.login(caller, GSSUtil.GSS_KRB5_MECH_OID);
                ticket = SubjectComber.find(loginSubj,
                    serverPrincipal, clientPrincipal, KerberosTicket.class);
                if (ticket != null) {
                    return ticket; 
                }
            } catch (LoginException e) {
            }
        }
        KerberosTicket tgt = SubjectComber.find(accSubj,
            tgsPrincipal, clientPrincipal, KerberosTicket.class);
        boolean fromAcc;
        if (tgt == null && loginSubj != null) {
            tgt = SubjectComber.find(loginSubj,
                tgsPrincipal, clientPrincipal, KerberosTicket.class);
            fromAcc = false;
        } else {
            fromAcc = true;
        }
        if (tgt != null) {
            Credentials tgtCreds = ticketToCreds(tgt);
            Credentials serviceCreds = Credentials.acquireServiceCreds(
                        serverPrincipal, tgtCreds);
            if (serviceCreds != null) {
                ticket = credsToTicket(serviceCreds);
                if (fromAcc && accSubj != null && !accSubj.isReadOnly()) {
                    accSubj.getPrivateCredentials().add(ticket);
                }
            }
        }
        return ticket;
    }
    static KerberosTicket getTicket(GSSCaller caller,
        String clientPrincipal, String serverPrincipal,
        AccessControlContext acc) throws LoginException {
        Subject accSubj = Subject.getSubject(acc);
        KerberosTicket ticket =
            SubjectComber.find(accSubj, serverPrincipal, clientPrincipal,
                  KerberosTicket.class);
        if (ticket == null && !GSSUtil.useSubjectCredsOnly(caller)) {
            Subject subject = GSSUtil.login(caller, GSSUtil.GSS_KRB5_MECH_OID);
            ticket = SubjectComber.find(subject,
                serverPrincipal, clientPrincipal, KerberosTicket.class);
        }
        return ticket;
    }
    public static Subject getSubject(GSSCaller caller,
        AccessControlContext acc) throws LoginException {
        Subject subject = Subject.getSubject(acc);
        if (subject == null && !GSSUtil.useSubjectCredsOnly(caller)) {
            subject = GSSUtil.login(caller, GSSUtil.GSS_KRB5_MECH_OID);
        }
        return subject;
    }
    public static class KeysFromKeyTab extends KerberosKey {
        public KeysFromKeyTab(KerberosKey key) {
            super(key.getPrincipal(), key.getEncoded(),
                    key.getKeyType(), key.getVersionNumber());
        }
    }
    public static class ServiceCreds {
        private KerberosPrincipal kp;
        private List<KeyTab> ktabs;
        private List<KerberosKey> kk;
        private Subject subj;
        private static ServiceCreds getInstance(
                Subject subj, String serverPrincipal) {
            ServiceCreds sc = new ServiceCreds();
            sc.subj = subj;
            for (KerberosPrincipal p: subj.getPrincipals(KerberosPrincipal.class)) {
                if (serverPrincipal == null ||
                        p.getName().equals(serverPrincipal)) {
                    sc.kp = p;
                    serverPrincipal = p.getName();
                    break;
                }
            }
            if (sc.kp == null) {
                List<KerberosKey> keys = SubjectComber.findMany(
                        subj, null, null, KerberosKey.class);
                if (!keys.isEmpty()) {
                    sc.kp = keys.get(0).getPrincipal();
                    serverPrincipal = sc.kp.getName();
                    if (DEBUG) {
                        System.out.println(">>> ServiceCreds: no kp?"
                                + " find one from kk: " + serverPrincipal);
                    }
                } else {
                    return null;
                }
            }
            sc.ktabs = SubjectComber.findMany(
                        subj, null, null, KeyTab.class);
            sc.kk = SubjectComber.findMany(
                        subj, serverPrincipal, null, KerberosKey.class);
            if (sc.ktabs.isEmpty() && sc.kk.isEmpty()) {
                return null;
            }
            return sc;
        }
        public String getName() {
            return kp.getName();
        }
        public KerberosKey[] getKKeys() {
            if (ktabs.isEmpty()) {
                return kk.toArray(new KerberosKey[kk.size()]);
            } else {
                List<KerberosKey> keys = new ArrayList<>();
                for (KeyTab ktab: ktabs) {
                    for (KerberosKey k: ktab.getKeys(kp)) {
                        keys.add(k);
                    }
                }
                if (!subj.isReadOnly()) {
                    Set<Object> pcs = subj.getPrivateCredentials();
                    synchronized (pcs) {
                        Iterator<Object> iterator = pcs.iterator();
                        while (iterator.hasNext()) {
                            Object obj = iterator.next();
                            if (obj instanceof KeysFromKeyTab) {
                                KerberosKey key = (KerberosKey)obj;
                                if (Objects.equals(key.getPrincipal(), kp)) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                    for (KerberosKey key: keys) {
                        subj.getPrivateCredentials().add(new KeysFromKeyTab(key));
                    }
                }
                return keys.toArray(new KerberosKey[keys.size()]);
            }
        }
        public EncryptionKey[] getEKeys() {
            KerberosKey[] kkeys = getKKeys();
            EncryptionKey[] ekeys = new EncryptionKey[kkeys.length];
            for (int i=0; i<ekeys.length; i++) {
                ekeys[i] =  new EncryptionKey(
                            kkeys[i].getEncoded(), kkeys[i].getKeyType(),
                            new Integer(kkeys[i].getVersionNumber()));
            }
            return ekeys;
        }
        public void destroy() {
            kp = null;
            ktabs = null;
            kk = null;
        }
    }
    public static ServiceCreds getServiceCreds(GSSCaller caller,
        String serverPrincipal, AccessControlContext acc)
                throws LoginException {
        Subject accSubj = Subject.getSubject(acc);
        ServiceCreds sc = null;
        if (accSubj != null) {
            sc = ServiceCreds.getInstance(accSubj, serverPrincipal);
        }
        if (sc == null && !GSSUtil.useSubjectCredsOnly(caller)) {
            Subject subject = GSSUtil.login(caller, GSSUtil.GSS_KRB5_MECH_OID);
            sc = ServiceCreds.getInstance(subject, serverPrincipal);
        }
        return sc;
    }
    public static KerberosTicket credsToTicket(Credentials serviceCreds) {
        EncryptionKey sessionKey =  serviceCreds.getSessionKey();
        return new KerberosTicket(
            serviceCreds.getEncoded(),
            new KerberosPrincipal(serviceCreds.getClient().getName()),
            new KerberosPrincipal(serviceCreds.getServer().getName(),
                                KerberosPrincipal.KRB_NT_SRV_INST),
            sessionKey.getBytes(),
            sessionKey.getEType(),
            serviceCreds.getFlags(),
            serviceCreds.getAuthTime(),
            serviceCreds.getStartTime(),
            serviceCreds.getEndTime(),
            serviceCreds.getRenewTill(),
            serviceCreds.getClientAddresses());
    };
    public static Credentials ticketToCreds(KerberosTicket kerbTicket)
        throws KrbException, IOException {
        return new Credentials(
            kerbTicket.getEncoded(),
            kerbTicket.getClient().getName(),
            kerbTicket.getServer().getName(),
            kerbTicket.getSessionKey().getEncoded(),
            kerbTicket.getSessionKeyType(),
            kerbTicket.getFlags(),
            kerbTicket.getAuthTime(),
            kerbTicket.getStartTime(),
            kerbTicket.getEndTime(),
            kerbTicket.getRenewTill(),
            kerbTicket.getClientAddresses());
    }
    public static EncryptionKey[] keysFromJavaxKeyTab(
            KeyTab ktab, PrincipalName cname) {
        return SharedSecrets.getJavaxSecurityAuthKerberosAccess().
                keyTabGetEncryptionKeys(ktab, cname);
    }
}

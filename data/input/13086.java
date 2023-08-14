public final class KrbAsReqBuilder {
    private KDCOptions options;
    private PrincipalName cname;
    private PrincipalName sname;
    private KerberosTime from;
    private KerberosTime till;
    private KerberosTime rtime;
    private HostAddresses addresses;
    private final char[] password;
    private final KeyTab ktab;
    private PAData[] paList;        
    private KrbAsReq req;
    private KrbAsRep rep;
    private static enum State {
        INIT,       
        REQ_OK,     
        DESTROYED,  
    }
    private State state;
    private void init(PrincipalName cname)
            throws KrbException {
        if (cname.getRealm() == null) {
            cname.setRealm(Config.getInstance().getDefaultRealm());
        }
        this.cname = cname;
        state = State.INIT;
    }
    public KrbAsReqBuilder(PrincipalName cname, KeyTab ktab)
            throws KrbException {
        init(cname);
        this.ktab = ktab;
        this.password = null;
    }
    public KrbAsReqBuilder(PrincipalName cname, char[] pass)
            throws KrbException {
        init(cname);
        this.password = pass.clone();
        this.ktab = null;
    }
    public EncryptionKey[] getKeys() throws KrbException {
        checkState(State.REQ_OK, "Cannot get keys");
        if (password != null) {
            int[] eTypes = EType.getDefaults("default_tkt_enctypes");
            EncryptionKey[] result = new EncryptionKey[eTypes.length];
            String salt = null;     
            for (int i=0; i<eTypes.length; i++) {
                PAData.SaltAndParams snp =
                        PAData.getSaltAndParams(eTypes[i], paList);
                if (snp.salt != null) {
                    salt = snp.salt;
                    result[i] = EncryptionKey.acquireSecretKey(password,
                            snp.salt,
                            eTypes[i],
                            snp.params);
                }
            }
            if (salt == null) salt = cname.getSalt();
            for (int i=0; i<eTypes.length; i++) {
                if (result[i] == null) {
                    PAData.SaltAndParams snp =
                            PAData.getSaltAndParams(eTypes[i], paList);
                    result[i] = EncryptionKey.acquireSecretKey(password,
                            salt,
                            eTypes[i],
                            snp.params);
                }
            }
            return result;
        } else {
            throw new IllegalStateException("Required password not provided");
        }
    }
    public void setOptions(KDCOptions options) {
        checkState(State.INIT, "Cannot specify options");
        this.options = options;
    }
    public void setTarget(PrincipalName sname) {
        checkState(State.INIT, "Cannot specify target");
        this.sname = sname;
    }
    public void setAddresses(HostAddresses addresses) {
        checkState(State.INIT, "Cannot specify addresses");
        this.addresses = addresses;
    }
    private KrbAsReq build(EncryptionKey key) throws KrbException, IOException {
        int[] eTypes;
        if (password != null) {
            eTypes = EType.getDefaults("default_tkt_enctypes");
        } else {
            EncryptionKey[] ks = Krb5Util.keysFromJavaxKeyTab(ktab, cname);
            eTypes = EType.getDefaults("default_tkt_enctypes",
                    ks);
            for (EncryptionKey k: ks) k.destroy();
        }
        return new KrbAsReq(key,
            options,
            cname,
            sname,
            from,
            till,
            rtime,
            eTypes,
            addresses);
    }
    private KrbAsReqBuilder resolve()
            throws KrbException, Asn1Exception, IOException {
        if (ktab != null) {
            rep.decryptUsingKeyTab(ktab, req, cname);
        } else {
            rep.decryptUsingPassword(password, req, cname);
        }
        if (rep.getPA() != null) {
            if (paList == null || paList.length == 0) {
                paList = rep.getPA();
            } else {
                int extraLen = rep.getPA().length;
                if (extraLen > 0) {
                    int oldLen = paList.length;
                    paList = Arrays.copyOf(paList, paList.length + extraLen);
                    System.arraycopy(rep.getPA(), 0, paList, oldLen, extraLen);
                }
            }
        }
        return this;
    }
    private KrbAsReqBuilder send() throws KrbException, IOException {
        boolean preAuthFailedOnce = false;
        KdcComm comm = new KdcComm(cname.getRealmAsString());
        EncryptionKey pakey = null;
        while (true) {
            try {
                req = build(pakey);
                rep = new KrbAsRep(comm.send(req.encoding()));
                return this;
            } catch (KrbException ke) {
                if (!preAuthFailedOnce && (
                        ke.returnCode() == Krb5.KDC_ERR_PREAUTH_FAILED ||
                        ke.returnCode() == Krb5.KDC_ERR_PREAUTH_REQUIRED)) {
                    if (Krb5.DEBUG) {
                        System.out.println("KrbAsReqBuilder: " +
                                "PREAUTH FAILED/REQ, re-send AS-REQ");
                    }
                    preAuthFailedOnce = true;
                    KRBError kerr = ke.getError();
                    if (password == null) {
                        EncryptionKey[] ks = Krb5Util.keysFromJavaxKeyTab(ktab, cname);
                        pakey = EncryptionKey.findKey(kerr.getEType(), ks);
                        if (pakey != null) pakey = (EncryptionKey)pakey.clone();
                        for (EncryptionKey k: ks) k.destroy();
                    } else {
                        PAData.SaltAndParams snp = PAData.getSaltAndParams(
                                kerr.getEType(), kerr.getPA());
                        if (kerr.getEType() == 0) {
                            pakey = EncryptionKey.acquireSecretKey(password,
                                    snp.salt == null ? cname.getSalt() : snp.salt,
                                    EType.getDefaults("default_tkt_enctypes")[0],
                                    null);
                        } else {
                            pakey = EncryptionKey.acquireSecretKey(password,
                                    snp.salt == null ? cname.getSalt() : snp.salt,
                                    kerr.getEType(),
                                    snp.params);
                        }
                    }
                    paList = kerr.getPA();  
                } else {
                    throw ke;
                }
            }
        }
    }
    public KrbAsReqBuilder action()
            throws KrbException, Asn1Exception, IOException {
        checkState(State.INIT, "Cannot call action");
        state = State.REQ_OK;
        return send().resolve();
    }
    public Credentials getCreds() {
        checkState(State.REQ_OK, "Cannot retrieve creds");
        return rep.getCreds();
    }
    public sun.security.krb5.internal.ccache.Credentials getCCreds() {
        checkState(State.REQ_OK, "Cannot retrieve CCreds");
        return rep.getCCreds();
    }
    public void destroy() {
        state = State.DESTROYED;
        if (password != null) {
            Arrays.fill(password, (char)0);
        }
    }
    private void checkState(State st, String msg) {
        if (state != st) {
            throw new IllegalStateException(msg + " at " + st + " state");
        }
    }
}

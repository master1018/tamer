public class KrbCred {
    private static boolean DEBUG = Krb5.DEBUG;
    private byte[] obuf = null;
    private KRBCred credMessg = null;
    private Ticket ticket = null;
    private EncKrbCredPart encPart = null;
    private Credentials creds = null;
    private KerberosTime timeStamp = null;
    public KrbCred(Credentials tgt,
                   Credentials serviceTicket,
                   EncryptionKey key)
        throws KrbException, IOException {
        PrincipalName client = tgt.getClient();
        PrincipalName tgService = tgt.getServer();
        PrincipalName server = serviceTicket.getServer();
        if (!serviceTicket.getClient().equals(client))
            throw new KrbException(Krb5.KRB_ERR_GENERIC,
                                "Client principal does not match");
        KDCOptions options = new KDCOptions();
        options.set(KDCOptions.FORWARDED, true);
        options.set(KDCOptions.FORWARDABLE, true);
        HostAddresses sAddrs = null;
        if (server.getNameType() == PrincipalName.KRB_NT_SRV_HST)
            sAddrs=  new HostAddresses(server);
        KrbTgsReq tgsReq = new KrbTgsReq(options, tgt, tgService,
                                         null, null, null, null, sAddrs, null, null, null);
        credMessg = createMessage(tgsReq.sendAndGetCreds(), key);
        obuf = credMessg.asn1Encode();
    }
    KRBCred createMessage(Credentials delegatedCreds, EncryptionKey key)
        throws KrbException, IOException {
        EncryptionKey sessionKey
            = delegatedCreds.getSessionKey();
        PrincipalName princ = delegatedCreds.getClient();
        Realm realm = princ.getRealm();
        PrincipalName tgService = delegatedCreds.getServer();
        Realm tgsRealm = tgService.getRealm();
        KrbCredInfo credInfo = new KrbCredInfo(sessionKey, realm,
                                               princ, delegatedCreds.flags, delegatedCreds.authTime,
                                               delegatedCreds.startTime, delegatedCreds.endTime,
                                               delegatedCreds.renewTill, tgsRealm, tgService,
                                               delegatedCreds.cAddr);
        timeStamp = new KerberosTime(KerberosTime.NOW);
        KrbCredInfo[] credInfos = {credInfo};
        EncKrbCredPart encPart =
            new EncKrbCredPart(credInfos,
                               timeStamp, null, null, null, null);
        EncryptedData encEncPart = new EncryptedData(key,
            encPart.asn1Encode(), KeyUsage.KU_ENC_KRB_CRED_PART);
        Ticket[] tickets = {delegatedCreds.ticket};
        credMessg = new KRBCred(tickets, encEncPart);
        return credMessg;
    }
    public KrbCred(byte[] asn1Message, EncryptionKey key)
        throws KrbException, IOException {
        credMessg = new KRBCred(asn1Message);
        ticket = credMessg.tickets[0];
        byte[] temp = credMessg.encPart.decrypt(key,
            KeyUsage.KU_ENC_KRB_CRED_PART);
        byte[] plainText = credMessg.encPart.reset(temp);
        DerValue encoding = new DerValue(plainText);
        EncKrbCredPart encPart = new EncKrbCredPart(encoding);
        timeStamp = encPart.timeStamp;
        KrbCredInfo credInfo = encPart.ticketInfo[0];
        EncryptionKey credInfoKey = credInfo.key;
        Realm prealm = credInfo.prealm;
        PrincipalName pname = credInfo.pname;
        pname.setRealm(prealm);
        TicketFlags flags = credInfo.flags;
        KerberosTime authtime = credInfo.authtime;
        KerberosTime starttime = credInfo.starttime;
        KerberosTime endtime = credInfo.endtime;
        KerberosTime renewTill = credInfo.renewTill;
        Realm srealm = credInfo.srealm;
        PrincipalName sname = credInfo.sname;
        sname.setRealm(srealm);
        HostAddresses caddr = credInfo.caddr;
        if (DEBUG) {
            System.out.println(">>>Delegated Creds have pname=" + pname
                               + " sname=" + sname
                               + " authtime=" + authtime
                               + " starttime=" + starttime
                               + " endtime=" + endtime
                               + "renewTill=" + renewTill);
        }
        creds = new Credentials(ticket, pname, sname, credInfoKey,
                                flags, authtime, starttime, endtime, renewTill, caddr);
    }
    public Credentials[] getDelegatedCreds() {
        Credentials[] allCreds = {creds};
        return allCreds;
    }
    public byte[] getMessage() {
        return obuf;
    }
}

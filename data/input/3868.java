public class CredentialsUtil {
    private static boolean DEBUG = sun.security.krb5.internal.Krb5.DEBUG;
    public static Credentials acquireServiceCreds(
                String service, Credentials ccreds)
    throws KrbException, IOException {
        ServiceName sname = new ServiceName(service);
        String serviceRealm = sname.getRealmString();
        String localRealm = ccreds.getClient().getRealmString();
        String defaultRealm = Config.getInstance().getDefaultRealm();
        if (localRealm == null) {
            PrincipalName temp = null;
            if ((temp = ccreds.getServer()) != null)
                localRealm = temp.getRealmString();
        }
        if (localRealm == null) {
            localRealm = defaultRealm;
        }
        if (serviceRealm == null) {
            serviceRealm = localRealm;
            sname.setRealm(serviceRealm);
        }
        if (localRealm.equals(serviceRealm))
        {
            if (DEBUG)
                System.out.println(">>> Credentials acquireServiceCreds: same realm");
            return serviceCreds(sname, ccreds);
        }
        String[] realms = Realm.getRealmsList(localRealm, serviceRealm);
        boolean okAsDelegate = true;
        if (realms == null || realms.length == 0)
        {
            if (DEBUG)
                System.out.println(">>> Credentials acquireServiceCreds: no realms list");
            return null;
        }
        int i = 0, k = 0;
        Credentials cTgt = null, newTgt = null, theTgt = null;
        ServiceName tempService = null;
        String realm = null, newTgtRealm = null, theTgtRealm = null;
        for (cTgt = ccreds, i = 0; i < realms.length;)
        {
            tempService = new ServiceName(PrincipalName.TGS_DEFAULT_SRV_NAME,
                                          serviceRealm, realms[i]);
            if (DEBUG)
            {
                System.out.println(">>> Credentials acquireServiceCreds: main loop: [" + i +"] tempService=" + tempService);
            }
            try {
                newTgt = serviceCreds(tempService, cTgt);
            } catch (Exception exc) {
                newTgt = null;
            }
            if (newTgt == null)
            {
                if (DEBUG)
                {
                    System.out.println(">>> Credentials acquireServiceCreds: no tgt; searching backwards");
                }
                for (newTgt = null, k = realms.length - 1;
                     newTgt == null && k > i; k--)
                {
                    tempService = new ServiceName(
                                       PrincipalName.TGS_DEFAULT_SRV_NAME,
                                       realms[k], realms[i]);
                    if (DEBUG)
                    {
                        System.out.println(">>> Credentials acquireServiceCreds: inner loop: [" + k +"] tempService=" + tempService);
                    }
                    try {
                        newTgt = serviceCreds(tempService, cTgt);
                    } catch (Exception exc) {
                        newTgt = null;
                    }
                }
            } 
            if (newTgt == null)
            {
                if (DEBUG)
                {
                    System.out.println(">>> Credentials acquireServiceCreds: no tgt; cannot get creds");
                }
                break;
            }
            newTgtRealm = newTgt.getServer().getInstanceComponent();
            if (okAsDelegate && !newTgt.checkDelegate()) {
                if (DEBUG)
                {
                    System.out.println(">>> Credentials acquireServiceCreds: " +
                            "global OK-AS-DELEGATE turned off at " +
                            newTgt.getServer());
                }
                okAsDelegate = false;
            }
            if (DEBUG)
            {
                System.out.println(">>> Credentials acquireServiceCreds: got tgt");
            }
            if (newTgtRealm.equals(serviceRealm))
            {
                theTgt = newTgt;
                theTgtRealm = newTgtRealm;
                break;
            }
            for (k = i+1; k < realms.length; k++)
            {
                if (newTgtRealm.equals(realms[k]))
                {
                    break;
                }
            }
            if (k < realms.length)
            {
                i = k;
                cTgt = newTgt;
                if (DEBUG)
                {
                    System.out.println(">>> Credentials acquireServiceCreds: continuing with main loop counter reset to " + i);
                }
                continue;
            }
            else
            {
                break;
            }
        } 
        Credentials theCreds = null;
        if (theTgt != null)
        {
            if (DEBUG)
            {
                System.out.println(">>> Credentials acquireServiceCreds: got right tgt");
                System.out.println(">>> Credentials acquireServiceCreds: obtaining service creds for " + sname);
            }
            try {
                theCreds = serviceCreds(sname, theTgt);
            } catch (Exception exc) {
              if (DEBUG)
                System.out.println(exc);
              theCreds = null;
            }
        }
        if (theCreds != null)
        {
            if (DEBUG)
            {
                System.out.println(">>> Credentials acquireServiceCreds: returning creds:");
                Credentials.printDebug(theCreds);
            }
            if (!okAsDelegate) {
                theCreds.resetDelegate();
            }
            return theCreds;
        }
        throw new KrbApErrException(Krb5.KRB_AP_ERR_GEN_CRED,
                                    "No service creds");
    }
    private static Credentials serviceCreds(
            ServiceName service, Credentials ccreds)
            throws KrbException, IOException {
        return new KrbTgsReq(ccreds, service).sendAndGetCreds();
    }
}

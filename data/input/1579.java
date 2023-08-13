public class OkAsDelegate {
    public static void main(String[] args)
            throws Exception {
        OkAsDelegate ok = new OkAsDelegate();
        ok.go(
                Boolean.valueOf(args[0]),   
                Boolean.valueOf(args[1]),   
                Boolean.valueOf(args[2]),   
                Boolean.valueOf(args[3]),   
                Boolean.valueOf(args[4]),   
                Boolean.valueOf(args[5])    
                );
    }
    void go(
            boolean forwardable,
            boolean requestDelegState,
            boolean requestDelegPolicyState,
            boolean delegState,
            boolean delegPolicyState,
            boolean delegated
            ) throws Exception {
        OneKDC kdc = new OneKDC(null);
        kdc.setPolicy("ok-as-delegate",
                System.getProperty("test.kdc.policy.ok-as-delegate"));
        kdc.writeJAASConf();
        if (!forwardable) {
            KDC.saveConfig(OneKDC.KRB5_CONF, kdc,
                    "default_keytab_name = " + OneKDC.KTAB);
            Config.refresh();
        }
        Context c, s;
        c = Context.fromJAAS("client");
        s = Context.fromJAAS("server");
        Oid mech = GSSUtil.GSS_KRB5_MECH_OID;
        if (System.getProperty("test.spnego") != null) {
            mech = GSSUtil.GSS_SPNEGO_MECH_OID;
        }
        c.startAsClient(OneKDC.SERVER, mech);
        ExtendedGSSContext cx = (ExtendedGSSContext)c.x();
        cx.requestCredDeleg(requestDelegState);
        cx.requestDelegPolicy(requestDelegPolicyState);
        s.startAsServer(mech);
        ExtendedGSSContext sx = (ExtendedGSSContext)s.x();
        Context.handshake(c, s);
        if (cx.getCredDelegState() != delegState) {
            throw new Exception("Initiator cred state error");
        }
        if (sx.getCredDelegState() != delegState) {
            throw new Exception("Acceptor cred state error");
        }
        if (cx.getDelegPolicyState() != delegPolicyState) {
            throw new Exception("Initiator cred policy state error");
        }
        GSSCredential cred = null;
        try {
            cred = s.x().getDelegCred();
        } catch (GSSException e) {
        }
        if (delegated != (cred != null)) {
            throw new Exception("get cred error");
        }
    }
}

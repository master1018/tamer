public class NonMutualSpnego {
    public static void main(String[] args)
            throws Exception {
        new OneKDC(null).writeJAASConf();
        new NonMutualSpnego().go();
    }
    void go() throws Exception {
        Context c = Context.fromJAAS("client");
        Context s = Context.fromJAAS("server");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_SPNEGO_MECH_OID);
        c.x().requestMutualAuth(false);
        s.startAsServer(GSSUtil.GSS_SPNEGO_MECH_OID);
        Context.handshake(c, s);
        Context.transmit("i say high --", c, s);
        Context.transmit("   you say low", s, c);
        c.dispose();
        s.dispose();
    }
}

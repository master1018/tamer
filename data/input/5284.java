public class SPNEGO {
    public static void main(String[] args) throws Exception {
        new OneKDC(null).writeJAASConf();
        Context c, s;
        c = Context.fromJAAS("client");
        s = Context.fromJAAS("server");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_SPNEGO_MECH_OID);
        s.startAsServer(GSSUtil.GSS_SPNEGO_MECH_OID);
        Context.handshake(c, s);
        Context.transmit("i say high --", c, s);
        Context.transmit("   you say low", s, c);
        s.dispose();
        c.dispose();
    }
}

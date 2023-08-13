public class BasicKrb5Test {
    private static boolean conf = true;
    public static void main(String[] args)
            throws Exception {
        String etype = null;
        for (String arg: args) {
            if (arg.equals("-s")) Context.usingStream = true;
            else if(arg.equals("-C")) conf = false;
            else etype = arg;
        }
        new OneKDC(etype).writeJAASConf();
        System.out.println("Testing etype " + etype);
        if (etype != null && !EType.isSupported(Config.getInstance().getType(etype))) {
            System.out.println("Not supported.");
            return;
        }
        new BasicKrb5Test().go(OneKDC.SERVER, OneKDC.BACKEND);
    }
    void go(final String server, final String backend) throws Exception {
        Context c, s, s2, b;
        c = Context.fromJAAS("client");
        s = Context.fromJAAS("server");
        b = Context.fromJAAS("backend");
        c.startAsClient(server, GSSUtil.GSS_KRB5_MECH_OID);
        c.x().requestCredDeleg(true);
        c.x().requestConf(conf);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        c.status();
        s.status();
        Context.handshake(c, s);
        GSSName client = c.x().getSrcName();
        c.status();
        s.status();
        Context.transmit("i say high --", c, s);
        Context.transmit("   you say low", s, c);
        s2 = s.delegated();
        s.dispose();
        s = null;
        s2.startAsClient(backend, GSSUtil.GSS_KRB5_MECH_OID);
        s2.x().requestConf(conf);
        b.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s2.status();
        b.status();
        Context.handshake(s2, b);
        GSSName client2 = b.x().getSrcName();
        if (!client.equals(client2)) {
            throw new Exception("Delegation failed");
        }
        s2.status();
        b.status();
        Context.transmit("you say hello --", s2, b);
        Context.transmit("   i say goodbye", b, s2);
        s2.dispose();
        b.dispose();
    }
}

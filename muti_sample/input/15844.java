public class NoAddresses {
    public static void main(String[] args)
            throws Exception {
        OneKDC kdc = new OneKDC(null);
        kdc.writeJAASConf();
        KDC.saveConfig(OneKDC.KRB5_CONF, kdc,
                "noaddresses = false",
                "default_keytab_name = " + OneKDC.KTAB);
        Config.refresh();
        Context c = Context.fromJAAS("client");
        Context s = Context.fromJAAS("server");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        InetAddress initiator = InetAddress.getLocalHost();
        InetAddress acceptor = InetAddress.getLocalHost();
        switch (args[0]) {
            case "1":
                break;
            case "2":
                c.x().setChannelBinding(
                        new ChannelBinding(initiator, acceptor, null));
                s.x().setChannelBinding(
                        new ChannelBinding(initiator, acceptor, null));
                break;
            case "3":
                initiator = InetAddress.getByAddress(new byte[]{1,1,1,1});
                c.x().setChannelBinding(
                        new ChannelBinding(initiator, acceptor, null));
                s.x().setChannelBinding(
                        new ChannelBinding(initiator, acceptor, null));
                break;
        }
        Context.handshake(c, s);
    }
}

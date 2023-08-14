public class IgnoreChannelBinding {
    public static void main(String[] args)
            throws Exception {
        new OneKDC(null).writeJAASConf();
        Context c = Context.fromJAAS("client");
        Context s = Context.fromJAAS("server");
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        c.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[0]
                ));
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Context.handshake(c, s);
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        c.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[0]
                ));
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[0]
                ));
        Context.handshake(c, s);
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        c.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[0]
                ));
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[1]     
                ));
        try {
            Context.handshake(c, s);
            throw new Exception("Acceptor should reject initiator");
        } catch (GSSException ge) {
        }
        c.startAsClient(OneKDC.SERVER, GSSUtil.GSS_KRB5_MECH_OID);
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        s.x().setChannelBinding(new ChannelBinding(
                InetAddress.getByName("client.rabbit.hole"),
                InetAddress.getByName("host.rabbit.hole"),
                new byte[0]
                ));
        try {
            Context.handshake(c, s);
            throw new Exception("Acceptor should reject initiator");
        } catch (GSSException ge) {
            if (ge.getMajor() != GSSException.BAD_BINDINGS) {
                throw ge;
            }
        }
    }
}

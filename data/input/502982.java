class L2tpIpsecPskService extends VpnService<L2tpIpsecPskProfile> {
    private static final String IPSEC = "racoon";
    @Override
    protected void connect(String serverIp, String username, String password)
            throws IOException {
        L2tpIpsecPskProfile p = getProfile();
        VpnDaemons daemons = getDaemons();
        daemons.startIpsecForL2tp(serverIp, p.getPresharedKey())
                .closeControlSocket();
        sleep(2000); 
        daemons.startL2tp(serverIp,
                (p.isSecretEnabled() ? p.getSecretString() : null),
                username, password);
    }
}

class L2tpIpsecService extends VpnService<L2tpIpsecProfile> {
    private static final String IPSEC = "racoon";
    @Override
    protected void connect(String serverIp, String username, String password)
            throws IOException {
        L2tpIpsecProfile p = getProfile();
        VpnDaemons daemons = getDaemons();
        DaemonProxy ipsec = daemons.startIpsecForL2tp(serverIp,
                Credentials.USER_PRIVATE_KEY + p.getUserCertificate(),
                Credentials.USER_CERTIFICATE + p.getUserCertificate(),
                Credentials.CA_CERTIFICATE + p.getCaCertificate());
        ipsec.closeControlSocket();
        sleep(2000); 
        daemons.startL2tp(serverIp,
                (p.isSecretEnabled() ? p.getSecretString() : null),
                username, password);
    }
}

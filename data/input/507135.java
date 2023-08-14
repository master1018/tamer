class L2tpService extends VpnService<L2tpProfile> {
    @Override
    protected void connect(String serverIp, String username, String password)
            throws IOException {
        L2tpProfile p = getProfile();
        getDaemons().startL2tp(serverIp,
                (p.isSecretEnabled() ? p.getSecretString() : null),
                username, password);
    }
}

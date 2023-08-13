class PptpService extends VpnService<PptpProfile> {
    @Override
    protected void connect(String serverIp, String username, String password)
            throws IOException {
        PptpProfile p = getProfile();
        getDaemons().startPptp(serverIp, username, password,
                p.isEncryptionEnabled());
    }
}

public final class DNSNameServiceDescriptor implements NameServiceDescriptor {
    public NameService createNameService() throws Exception {
        return new DNSNameService();
    }
    public String getProviderName() {
        return "sun";
    }
    public String getType() {
        return "dns";
    }
}

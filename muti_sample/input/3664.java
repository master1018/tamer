public final class SimpleNameServiceDescriptor implements NameServiceDescriptor {
    public NameService createNameService() throws Exception {
        return new SimpleNameService();
    }
    public String getProviderName() {
        return "sun";
    }
    public String getType() {
        return "simple";
    }
}

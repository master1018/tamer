public class ThrowingNameServiceDescriptor implements NameServiceDescriptor {
    public NameService createNameService() {
        return new ThrowingNameService();
    }
    @Override
    public String getProviderName() {
        return "sun";
    }
    @Override
    public String getType() {
        return "throwing";
    }
}

public class Simple1NameServiceDescriptor implements NameServiceDescriptor {
    public NameService createNameService() {
        SimpleNameService ns = new SimpleNameService();
        ns.put("blade", "10.0.0.1");
        ns.put("blade.domain1", "10.0.0.2");
        return ns;
    }
    public String getProviderName() {
        return "sun";
    }
    public String getType() {
        return "simple1";
    }
}

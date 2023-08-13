public class KeyStoreBuilderParameters implements ManagerFactoryParameters {
    private final List<KeyStore.Builder> ksbuilders;
    public KeyStoreBuilderParameters(KeyStore.Builder builder) {
        super();
        ksbuilders = Collections.singletonList(builder);
    }
    @SuppressWarnings("unchecked")
    public KeyStoreBuilderParameters(List parameters) {
        super();
        if (parameters == null) {
            throw new NullPointerException("Builders list is null");
        }
        if (parameters.isEmpty()) {
            throw new IllegalArgumentException("Builders list is empty");
        }
        ksbuilders = Collections.unmodifiableList(new ArrayList<KeyStore.Builder>(parameters));
    }
    @SuppressWarnings("unchecked")
    public List getParameters() {
        return ksbuilders;
    }
}

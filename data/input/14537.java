public class KeyStoreBuilderParameters implements ManagerFactoryParameters {
    private final List<Builder> parameters;
    public KeyStoreBuilderParameters(Builder builder) {
        parameters = Collections.singletonList(Objects.requireNonNull(builder));
    }
    public KeyStoreBuilderParameters(List<Builder> parameters) {
        if (parameters.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.parameters = Collections.unmodifiableList(
            new ArrayList<Builder>(parameters));
    }
    public List<Builder> getParameters() {
        return parameters;
    }
}

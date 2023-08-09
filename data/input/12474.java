public class NullProviderFactory extends ProviderFactory {
    public <T extends Provider> T createProvider(Class<T> cls) {
        NullProvider provider = new NullProvider(cls);
        provider.init();
        return provider.newProxyInstance();
    }
}
class NullProvider extends ProviderSkeleton {
    NullProvider(Class<? extends Provider> type) {
        super(type);
    }
    protected ProbeSkeleton createProbe(Method m) {
        return new NullProbe(m.getParameterTypes());
    }
}
class NullProbe extends ProbeSkeleton {
    public NullProbe(Class<?>[] parameters) {
        super(parameters);
    }
    public boolean isEnabled() {
        return false;
    }
    public void uncheckedTrigger(Object[] args) {
    }
}

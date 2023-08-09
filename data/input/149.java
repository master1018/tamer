public class MultiplexProviderFactory extends ProviderFactory {
    private Set<ProviderFactory> factories;
    public MultiplexProviderFactory(Set<ProviderFactory> factories) {
        this.factories = factories;
    }
    public <T extends Provider> T createProvider(Class<T> cls) {
        HashSet<Provider> providers = new HashSet<Provider>();
        for (ProviderFactory factory : factories) {
            providers.add(factory.createProvider(cls));
        }
        MultiplexProvider provider = new MultiplexProvider(cls, providers);
        provider.init();
        return provider.newProxyInstance();
    }
}
class MultiplexProvider extends ProviderSkeleton {
    private Set<Provider> providers;
    protected ProbeSkeleton createProbe(Method m) {
        return new MultiplexProbe(m, providers);
    }
    MultiplexProvider(Class<? extends Provider> type, Set<Provider> providers) {
        super(type);
        this.providers = providers;
    }
    public void dispose() {
        for (Provider p : providers) {
            p.dispose();
        }
        super.dispose();
    }
}
class MultiplexProbe extends ProbeSkeleton {
    private Set<Probe> probes;
    MultiplexProbe(Method m, Set<Provider> providers) {
        super(m.getParameterTypes());
        probes = new HashSet<Probe>();
        for (Provider p : providers) {
            Probe probe = p.getProbe(m);
            if (probe != null) {
                probes.add(probe);
            }
        }
    }
    public boolean isEnabled() {
        for (Probe p : probes) {
            if (p.isEnabled()) {
                return true;
            }
        }
        return false;
    }
    public void uncheckedTrigger(Object[] args) {
        for (Probe p : probes) {
            try {
                ProbeSkeleton ps = (ProbeSkeleton)p;
                ps.uncheckedTrigger(args);
            } catch (ClassCastException e) {
                try {
                    Method m = Probe.class.getMethod(
                        "trigger", Class.forName("[java.lang.Object"));
                    m.invoke(p, args);
                } catch (Exception e1) {
                    assert false; 
                }
            }
        }
    }
}

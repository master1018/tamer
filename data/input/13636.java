public class FilterChainProviderImplementation implements FilterChainProvider {
    public FilterChain getFilterChain() {
        return FilterTopComponent.findInstance().getFilterChain();
    }
    public FilterChain getSequence() {
        return FilterTopComponent.findInstance().getSequence();
    }
}

public class DefaultPoolMetadaLocator implements PoolMetadataLocator {
    public boolean isPoolMetadata(BusinessService service) {
        return (service instanceof ObjectPoolingAware);
    }
    public PoolMetadata lookupPoolMetadata(BusinessService service) {
        ObjectPoolingAware objPooling = (ObjectPoolingAware) service;
        return objPooling.getPoolMetadata();
    }
}

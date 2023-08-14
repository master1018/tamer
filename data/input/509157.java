public abstract class IIOServiceProvider implements RegisterableService {
    protected String vendorName;
    protected String version;
    public IIOServiceProvider(String vendorName, String version) {
        if (vendorName == null) {
            throw new NullPointerException("vendor name cannot be NULL");
        }
        if (version == null) {
            throw new NullPointerException("version name cannot be NULL");
        }
        this.vendorName = vendorName;
        this.version = version;
    }
    public IIOServiceProvider() {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
    }
    public void onDeregistration(ServiceRegistry registry, Class<?> category) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public String getVendorName() {
        return vendorName;
    }
    public String getVersion() {
        return version;
    }
    public abstract String getDescription(Locale locale);
}

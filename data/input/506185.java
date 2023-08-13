public class Engine {
    private String serviceName;
    private Provider.Service returnedService;
    private String lastAlgorithm;
    private int refreshNumber;
    public Provider provider;
    public Object spi;
    public static SecurityAccess door;
    public Engine(String service) {
        this.serviceName = service;
    }
    public synchronized void getInstance(String algorithm, Object param)
            throws NoSuchAlgorithmException {
        Provider.Service serv;
        if (algorithm == null) {
            throw new NoSuchAlgorithmException(Messages.getString("security.149")); 
        }
        Services.refresh();
        if (returnedService != null
                && Util.equalsIgnoreCase(algorithm, lastAlgorithm)
                && refreshNumber == Services.refreshNumber) {
            serv = returnedService;
        } else {
            if (Services.isEmpty()) {
                throw new NoSuchAlgorithmException(Messages.getString("security.14A", 
                        serviceName, algorithm));
            }
            serv = Services.getService(new StringBuilder(128)
                    .append(serviceName).append(".").append( 
                            Util.toUpperCase(algorithm)).toString());
            if (serv == null) {
                throw new NoSuchAlgorithmException(Messages.getString("security.14A", 
                        serviceName, algorithm));
            }
            returnedService = serv;
            lastAlgorithm = algorithm;
            refreshNumber = Services.refreshNumber;
        }
        spi = serv.newInstance(param);
        this.provider = serv.getProvider();
    }
    public synchronized void getInstance(String algorithm, Provider provider,
            Object param) throws NoSuchAlgorithmException {
        Provider.Service serv = null;
        if (algorithm == null) {
            throw new NoSuchAlgorithmException(
                    Messages.getString("security.14B", serviceName)); 
        }
        serv = provider.getService(serviceName, algorithm);
        if (serv == null) {
            throw new NoSuchAlgorithmException(Messages.getString("security.14A", 
                    serviceName, algorithm));
        }
        spi = serv.newInstance(param);
        this.provider = provider;
    }
}
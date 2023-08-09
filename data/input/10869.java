public class GetInstanceConfigSpi extends ConfigurationSpi {
    private Configuration c;
    public GetInstanceConfigSpi(final Configuration.Parameters params) {
        c = AccessController.doPrivileged
            (new PrivilegedAction<Configuration>() {
            public Configuration run() {
                if (params instanceof URIParameter) {
                    URIParameter uriParam = (URIParameter)params;
                    return new ConfigFile(uriParam.getURI());
                }
                return new ConfigFile();
            }
        });
    }
    public AppConfigurationEntry[] engineGetAppConfigurationEntry(String name) {
        return c.getAppConfigurationEntry(name);
    }
}

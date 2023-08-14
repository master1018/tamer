public final class ConfigSpiFile extends ConfigurationSpi {
    private ConfigFile cf;
    public ConfigSpiFile(final Configuration.Parameters params)
        throws java.io.IOException {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                if (params == null) {
                    cf = new ConfigFile();
                } else {
                    if (!(params instanceof URIParameter)) {
                        throw new IllegalArgumentException
                                ("Unrecognized parameter: " + params);
                    }
                    URIParameter uriParam = (URIParameter)params;
                    cf = new ConfigFile(uriParam.getURI());
                }
                return null;
            }
            });
        } catch (SecurityException se) {
            Throwable cause = se.getCause();
            if (cause != null && cause instanceof java.io.IOException) {
                throw (java.io.IOException)cause;
            }
            throw se;
        }
    }
    protected AppConfigurationEntry[] engineGetAppConfigurationEntry
                (String name) {
        return cf.getAppConfigurationEntry(name);
    }
    protected void engineRefresh() {
        cf.refresh();
    }
}

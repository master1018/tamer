public abstract class ConfigurationSpi {
    protected abstract AppConfigurationEntry[] engineGetAppConfigurationEntry
                                                        (String name);
    protected void engineRefresh() { }
}

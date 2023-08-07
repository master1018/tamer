public class ApacheLdapProviderConfig extends ProviderConfig {
    private LdapConnectionConfig connectionConfig;
    private ControlProcessor<Control> controlProcessor;
    public ApacheLdapProviderConfig() {
        controlProcessor = new ControlProcessor<Control>(new ApacheLdapControlHandler());
    }
    public LdapConnectionConfig getLdapConnectionConfig() {
        return connectionConfig;
    }
    public void setLdapConnectionConfig(final LdapConnectionConfig config) {
        checkImmutable();
        logger.trace("setting ldapConnectionConfig: {}", config);
        connectionConfig = config;
    }
    public ControlProcessor<Control> getControlProcessor() {
        return controlProcessor;
    }
    public void setControlProcessor(final ControlProcessor<Control> processor) {
        checkImmutable();
        logger.trace("setting controlProcessor: {}", processor);
        controlProcessor = processor;
    }
    @Override
    public String toString() {
        return String.format("[%s@%d::operationRetryResultCodes=%s, properties=%s, " + "connectionStrategy=%s, ldapConnectionConfig=%s, controlProcessor=%s]", getClass().getName(), hashCode(), Arrays.toString(getOperationRetryResultCodes()), getProperties(), getConnectionStrategy(), connectionConfig, controlProcessor);
    }
}

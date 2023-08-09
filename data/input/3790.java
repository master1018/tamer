public class MBeanServerBuilderImpl extends MBeanServerBuilder {
    private final MBeanServerBuilder inner;
    public MBeanServerBuilderImpl() {
        inner = new MBeanServerBuilder();
    }
    public MBeanServer newMBeanServer(String defaultDomain,
                                      MBeanServer outer,
                                      MBeanServerDelegate delegate) {
        final MBeanServerForwarder mbsf =
            MBeanServerForwarderInvocationHandler.newProxyInstance();
        final MBeanServer innerMBeanServer =
            inner.newMBeanServer(defaultDomain,
                                 (outer == null ? mbsf : outer),
                                 delegate);
        mbsf.setMBeanServer(innerMBeanServer);
        return mbsf;
    }
}

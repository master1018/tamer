public class MBeanServerBuilder {
    public MBeanServerBuilder() {
    }
    public MBeanServerDelegate newMBeanServerDelegate() {
        return JmxMBeanServer.newMBeanServerDelegate();
    }
    public MBeanServer newMBeanServer(String              defaultDomain,
                                      MBeanServer         outer,
                                      MBeanServerDelegate delegate) {
        return JmxMBeanServer.newMBeanServer(defaultDomain,outer,delegate,
                                             false);
    }
}

public class DefaultProviderTest {
    public static void main(String[] args) throws Exception {
        URLClassLoader emptyLoader = new URLClassLoader(new URL[0], null);
        Thread.currentThread().setContextClassLoader(emptyLoader);
        Map env = new HashMap();
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER,
                emptyLoader);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        JMXConnector conn = JMXConnectorFactory.newJMXConnector(url, env);
        System.out.println("Successfully created RMI connector in hostile " +
                           "class-loading environment");
    }
}

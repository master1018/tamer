public class PasswordAccessFileTest {
    public static void main(String[] args) {
        try {
            System.out.println("Create the MBean server");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            ObjectName mbeanName = new ObjectName("MBeans:type=SimpleStandard");
            System.out.println("Create SimpleStandard MBean...");
            mbs.createMBean("SimpleStandard", mbeanName, null, null);
            System.out.println(">>> Initialize the server's environment map");
            HashMap sEnv = new HashMap();
            sEnv.put("jmx.remote.x.password.file",
                     System.getProperty("test.src") +
                     File.separator +
                     "password.properties");
            sEnv.put("jmx.remote.x.access.file",
                     System.getProperty("test.src") +
                     File.separator +
                     "access.properties");
            System.out.println("Create an RMI connector server");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            JMXConnectorServer cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, sEnv, mbs);
            System.out.println("Start the RMI connector server");
            cs.start();
            System.out.println("RMI connector server successfully started");
            System.out.println("Waiting for incoming connections...");
            final String invalidCreds[][] = {
                {"admin1", "adminPassword"},
                {"admin",  "adminPassword1"},
                {"user1",  "userPassword"},
                {"user",   "userPassword1"}
            };
            for (int i = 0 ; i < invalidCreds.length ; i++) {
                System.out.println(">>> Initialize the client environment map" +
                                   " for user [" +
                                   invalidCreds[i][0] +
                                   "] with password [" +
                                   invalidCreds[i][1] + "]");
                HashMap cEnv = new HashMap();
                cEnv.put("jmx.remote.credentials", invalidCreds[i]);
                System.out.println("Create an RMI connector client and " +
                                   "connect it to the RMI connector server");
                try {
                    JMXConnector jmxc =
                        JMXConnectorFactory.connect(cs.getAddress(), cEnv);
                } catch (SecurityException e) {
                    System.out.println("Got expected security exception: " + e);
                } catch (Exception e) {
                    System.out.println("Got unexpected exception: " + e);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            String[] adminCreds = new String[] { "admin" , "adminPassword" };
            System.out.println(">>> Initialize the client environment map for" +
                               " user [" + adminCreds[0] + "] with " +
                               "password [" + adminCreds[1] + "]");
            HashMap adminEnv = new HashMap();
            adminEnv.put("jmx.remote.credentials", adminCreds);
            System.out.println("Create an RMI connector client and " +
                               "connect it to the RMI connector server");
            JMXConnector adminConnector =
                JMXConnectorFactory.connect(cs.getAddress(), adminEnv);
            System.out.println("Get an MBeanServerConnection");
            MBeanServerConnection adminConnection =
                adminConnector.getMBeanServerConnection();
            SimpleStandardMBean adminProxy = (SimpleStandardMBean)
                MBeanServerInvocationHandler.newProxyInstance(
                                             adminConnection,
                                             mbeanName,
                                             SimpleStandardMBean.class,
                                             false);
            System.out.println("State = " + adminProxy.getState());
            adminProxy.setState("changed state");
            System.out.println("State = " + adminProxy.getState());
            System.out.println("Invoke reset() in SimpleStandard MBean...");
            adminProxy.reset();
            System.out.println("Close the admin connection to the server");
            adminConnector.close();
            String[] userCreds = new String[] { "user" , "userPassword" };
            System.out.println(">>> Initialize the client environment map for" +
                               " user [" + userCreds[0] + "] with " +
                               "password [" + userCreds[1] + "]");
            HashMap userEnv = new HashMap();
            userEnv.put("jmx.remote.credentials", userCreds);
            System.out.println("Create an RMI connector client and " +
                               "connect it to the RMI connector server");
            JMXConnector userConnector =
                JMXConnectorFactory.connect(cs.getAddress(), userEnv);
            System.out.println("Get an MBeanServerConnection");
            MBeanServerConnection userConnection =
                userConnector.getMBeanServerConnection();
            SimpleStandardMBean userProxy = (SimpleStandardMBean)
                MBeanServerInvocationHandler.newProxyInstance(
                                             userConnection,
                                             mbeanName,
                                             SimpleStandardMBean.class,
                                             false);
            System.out.println("State = " + userProxy.getState());
            try {
                userProxy.setState("changed state");
            } catch (SecurityException e) {
                System.out.println("Got expected security exception: " + e);
            } catch (Exception e) {
                System.out.println("Got unexpected exception: " + e);
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("State = " + userProxy.getState());
            try {
                System.out.println("Invoke reset() in SimpleStandard MBean...");
                userProxy.reset();
            } catch (SecurityException e) {
                System.out.println("Got expected security exception: " + e);
            } catch (Exception e) {
                System.out.println("Got unexpected exception: " + e);
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("Close the user connection to the server");
            userConnector.close();
            System.out.println(">>> Stop the connector server");
            cs.stop();
            System.out.println("Bye! Bye!");
        } catch (Exception e) {
            System.out.println("Got unexpected exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

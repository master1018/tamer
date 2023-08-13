public class RMIAltAuthTest {
    public static void main(String[] args) {
        try {
            final String loginConfig = System.getProperty("test.src") +
                File.separator + "jmxremote.login";
            System.out.println("JAAS configuration file = " + loginConfig);
            System.setProperty("java.security.auth.login.config",
                "file:" + loginConfig);
            System.out.println("Start RMI registry...");
            Registry reg = null;
            int port = 5800;
            while (port++ < 6000) {
                try {
                    reg = LocateRegistry.createRegistry(port);
                    System.out.println("RMI registry running on port " + port);
                    break;
                } catch (RemoteException e) {
                    System.out.println("Failed to create RMI registry " +
                                       "on port " + port);
                }
            }
            if (reg == null) {
                System.exit(1);
            }
            System.out.println("Create the MBean server");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            System.out.println("Create ClassPathClassLoader MBean");
            ObjectName cpcl =
                new ObjectName("ClassLoader:name=ClassPathClassLoader");
            mbs.createMBean("javax.management.loading.MLet", cpcl);
            System.out.println("Create SimpleStandard MBean");
            mbs.createMBean("SimpleStandard",
                            new ObjectName("MBeans:name=SimpleStandard"));
            Properties props = new Properties();
            props.setProperty("jmx.remote.x.login.config",
                "PasswordFileAuthentication");
            System.out.println("Initialize environment map");
            HashMap env = new HashMap();
            env.put("jmx.remote.authenticator",
                    new JMXPluggableAuthenticator(props));
            System.out.println("Create an RMI connector server");
            JMXServiceURL url =
                new JMXServiceURL("rmi", null, 0,
                                  "/jndi/rmi:
            JMXConnectorServer rcs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            rcs.start();
            System.out.println("Create an RMI connector client");
            HashMap cli_env = new HashMap();
            String[] credentials = new String[] { "monitorRole" , "pwd1" };
            cli_env.put("jmx.remote.credentials", credentials);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, cli_env);
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            System.out.println("Domains:");
            String domains[] = mbsc.getDomains();
            for (int i = 0; i < domains.length; i++) {
                System.out.println("\tDomain[" + i + "] = " + domains[i]);
            }
            System.out.println("MBean count = " + mbsc.getMBeanCount());
            String oldState =
                (String) mbsc.getAttribute(
                              new ObjectName("MBeans:name=SimpleStandard"),
                              "State");
            System.out.println("Old State = \"" + oldState + "\"");
            System.out.println("Set State to \"changed state\"");
            mbsc.setAttribute(new ObjectName("MBeans:name=SimpleStandard"),
                              new Attribute("State", "changed state"));
            String newState =
                (String) mbsc.getAttribute(
                              new ObjectName("MBeans:name=SimpleStandard"),
                              "State");
            System.out.println("New State = \"" + newState + "\"");
            if (!newState.equals("changed state")) {
                System.out.println("Invalid State = \"" + newState + "\"");
                System.exit(1);
            }
            System.out.println("Add notification listener...");
            mbsc.addNotificationListener(
                 new ObjectName("MBeans:name=SimpleStandard"),
                 new NotificationListener() {
                     public void handleNotification(Notification notification,
                                                    Object handback) {
                         System.out.println("Received notification: " +
                                            notification);
                     }
                 },
                 null,
                 null);
            System.out.println("Unregister SimpleStandard MBean...");
            mbsc.unregisterMBean(new ObjectName("MBeans:name=SimpleStandard"));
            jmxc.close();
            System.out.println("Bye! Bye!");
        } catch (Exception e) {
            System.out.println("Unexpected exception caught = " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

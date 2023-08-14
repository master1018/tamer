public class SubjectDelegation3Test {
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        System.out.println("os.name = " + osName);
        if (!osName.equals("SunOS")) {
            System.out.println("This test runs on Solaris only.");
            System.out.println("Bye! Bye!");
            return;
        }
        String policyFile = args[0];
        String testResult = args[1];
        System.out.println("Policy file = " + policyFile);
        System.out.println("Expected test result = " + testResult);
        JMXConnectorServer jmxcs = null;
        JMXConnector jmxc = null;
        try {
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
            final String passwordFile = System.getProperty("test.src") +
                File.separator + "jmxremote.password";
            System.out.println("Password file = " + passwordFile);
            final String policy = System.getProperty("test.src") +
                File.separator + policyFile;
            System.out.println("PolicyFile = " + policy);
            System.setProperty("java.security.policy", policy);
            System.out.println("Create the MBean server");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            System.out.println("Create SimpleStandard MBean");
            SimpleStandard s = new SimpleStandard("delegate");
            mbs.registerMBean(s, new ObjectName("MBeans:type=SimpleStandard"));
            System.setSecurityManager(new SecurityManager());
            Properties props = new Properties();
            props.setProperty("jmx.remote.x.password.file", passwordFile);
            System.out.println("Initialize environment map");
            HashMap env = new HashMap();
            env.put("jmx.remote.authenticator",
                    new JMXPluggableAuthenticator(props));
            System.out.println("Create an RMI connector server");
            JMXServiceURL url =
                new JMXServiceURL("rmi", null, 0,
                                  "/jndi/rmi:
            jmxcs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            jmxcs.start();
            System.out.println("Create an RMI connector client");
            HashMap cli_env = new HashMap();
            String[] credentials = new String[] { "monitorRole" , "QED" };
            cli_env.put("jmx.remote.credentials", credentials);
            jmxc = JMXConnectorFactory.connect(url, cli_env);
            Subject delegationSubject =
                new Subject(true,
                            Collections.singleton(new JMXPrincipal("delegate")),
                            Collections.EMPTY_SET,
                            Collections.EMPTY_SET);
            MBeanServerConnection mbsc =
                jmxc.getMBeanServerConnection(delegationSubject);
            System.out.println("Domains:");
            String domains[] = mbsc.getDomains();
            for (int i = 0; i < domains.length; i++) {
                System.out.println("\tDomain[" + i + "] = " + domains[i]);
            }
            System.out.println("MBean count = " + mbsc.getMBeanCount());
            String oldState =
                (String) mbsc.getAttribute(
                              new ObjectName("MBeans:type=SimpleStandard"),
                              "State");
            System.out.println("Old State = \"" + oldState + "\"");
            System.out.println("Set State to \"changed state\"");
            mbsc.setAttribute(new ObjectName("MBeans:type=SimpleStandard"),
                              new Attribute("State", "changed state"));
            String newState =
                (String) mbsc.getAttribute(
                              new ObjectName("MBeans:type=SimpleStandard"),
                              "State");
            System.out.println("New State = \"" + newState + "\"");
            if (!newState.equals("changed state")) {
                System.out.println("Invalid State = \"" + newState + "\"");
                System.exit(1);
            }
            System.out.println("Add notification listener...");
            mbsc.addNotificationListener(
                 new ObjectName("MBeans:type=SimpleStandard"),
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
            mbsc.unregisterMBean(new ObjectName("MBeans:type=SimpleStandard"));
        } catch (SecurityException e) {
            if (testResult.equals("ko")) {
                System.out.println("Got expected security exception = " + e);
            } else {
                System.out.println("Got unexpected security exception = " + e);
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception caught = " + e);
            e.printStackTrace();
            throw e;
        } finally {
            if (jmxc != null)
                jmxc.close();
            if (jmxcs != null)
                jmxcs.stop();
            System.out.println("Bye! Bye!");
        }
    }
}

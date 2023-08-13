public class RMIExporterTest {
    public static class CustomRMIExporter implements RMIExporter {
        public boolean rmiServerExported = false;
        public boolean rmiServerUnexported = false;
        public boolean rmiConnectionExported = false;
        public boolean rmiConnectionUnexported = false;
        public Remote exportObject(Remote obj,
                                   int port,
                                   RMIClientSocketFactory csf,
                                   RMIServerSocketFactory ssf)
            throws RemoteException {
            System.out.println("CustomRMIExporter::exportObject():: " +
                               "Remote = " + obj);
            if (obj.toString().startsWith(
                    "javax.management.remote.rmi.RMIJRMPServerImpl"))
                rmiServerExported = true;
            if (obj.toString().startsWith(
                    "javax.management.remote.rmi.RMIConnectionImpl"))
                rmiConnectionExported = true;
            return UnicastRemoteObject.exportObject(obj, port, csf, ssf);
        }
        public boolean unexportObject(Remote obj, boolean force)
            throws NoSuchObjectException {
            System.out.println("CustomRMIExporter::unexportObject():: " +
                               "Remote = " + obj);
            if (obj.toString().startsWith(
                    "javax.management.remote.rmi.RMIJRMPServerImpl"))
                rmiServerUnexported = true;
            if (obj.toString().startsWith(
                    "javax.management.remote.rmi.RMIConnectionImpl"))
                rmiConnectionUnexported = true;
            return UnicastRemoteObject.unexportObject(obj, force);
        }
    }
    public static void main(String[] args) {
        try {
            System.out.println("Create the MBean server");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            System.out.println("Initialize environment map");
            HashMap env = new HashMap();
            CustomRMIExporter exporter = new CustomRMIExporter();
            env.put(RMIExporter.EXPORTER_ATTRIBUTE, exporter);
            System.out.println("Create an RMI connector server");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            JMXConnectorServer cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            cs.start();
            System.out.println("Create an RMI connector client");
            JMXConnector cc =
                JMXConnectorFactory.connect(cs.getAddress(), null);
            System.out.println("Close the RMI connector client");
            cc.close();
            System.out.println("Stop the RMI connector server");
            cs.stop();
            int errorCount = 0;
            if (exporter.rmiServerExported) {
                System.out.println("RMIServer exported OK!");
            } else {
                System.out.println("RMIServer exported KO!");
                errorCount++;
            }
            if (exporter.rmiServerUnexported) {
                System.out.println("RMIServer unexported OK!");
            } else {
                System.out.println("RMIServer unexported KO!");
                errorCount++;
            }
            if (exporter.rmiConnectionExported) {
                System.out.println("RMIConnection exported OK!");
            } else {
                System.out.println("RMIConnection exported KO!");
                errorCount++;
            }
            if (exporter.rmiConnectionUnexported) {
                System.out.println("RMIConnection unexported OK!");
            } else {
                System.out.println("RMIConnection unexported KO!");
                errorCount++;
            }
            System.out.println("Bye! Bye!");
            if (errorCount > 0) {
                System.out.println("RMIExporterTest FAILED!");
                System.exit(1);
            } else {
                System.out.println("RMIExporterTest PASSED!");
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception caught = " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

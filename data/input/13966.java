public class Jstatd {
    private static Registry registry;
    private static int port = -1;
    private static boolean startRegistry = true;
    private static void printUsage() {
        System.err.println("usage: jstatd [-nr] [-p port] [-n rminame]");
    }
    static void bind(String name, RemoteHostImpl remoteHost)
                throws RemoteException, MalformedURLException, Exception {
        try {
            Naming.rebind(name, remoteHost);
        } catch (java.rmi.ConnectException e) {
            if (startRegistry && registry == null) {
                int localport = (port < 0) ? Registry.REGISTRY_PORT : port;
                registry = LocateRegistry.createRegistry(localport);
                bind(name, remoteHost);
            }
            else {
                System.out.println("Could not contact registry\n"
                                   + e.getMessage());
                e.printStackTrace();
            }
        } catch (RemoteException e) {
            System.err.println("Could not bind " + name + " to RMI Registry");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String rminame = null;
        int argc = 0;
        for ( ; (argc < args.length) && (args[argc].startsWith("-")); argc++) {
            String arg = args[argc];
            if (arg.compareTo("-nr") == 0) {
                startRegistry = false;
            } else if (arg.startsWith("-p")) {
                if (arg.compareTo("-p") != 0) {
                    port = Integer.parseInt(arg.substring(2));
                } else {
                  argc++;
                  if (argc >= args.length) {
                      printUsage();
                      System.exit(1);
                  }
                  port = Integer.parseInt(args[argc]);
                }
            } else if (arg.startsWith("-n")) {
                if (arg.compareTo("-n") != 0) {
                    rminame = arg.substring(2);
                } else {
                    argc++;
                    if (argc >= args.length) {
                        printUsage();
                        System.exit(1);
                    }
                    rminame = args[argc];
                }
            } else {
                printUsage();
                System.exit(1);
            }
        }
        if (argc < args.length) {
            printUsage();
            System.exit(1);
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        StringBuilder name = new StringBuilder();
        if (port >= 0) {
            name.append("
        }
        if (rminame == null) {
            rminame = "JStatRemoteHost";
        }
        name.append("/").append(rminame);
        try {
            System.setProperty("java.rmi.server.ignoreSubClasses", "true");
            RemoteHostImpl remoteHost = new RemoteHostImpl();
            RemoteHost stub = (RemoteHost) UnicastRemoteObject.exportObject(
                    remoteHost, 0);
            bind(name.toString(), remoteHost);
        } catch (MalformedURLException e) {
            if (rminame != null) {
                System.out.println("Bad RMI server name: " + rminame);
            } else {
                System.out.println("Bad RMI URL: " + name + " : "
                                   + e.getMessage());
            }
            System.exit(1);
        } catch (java.rmi.ConnectException e) {
            System.out.println("Could not contact RMI registry\n"
                               + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Could not create remote object\n"
                               + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

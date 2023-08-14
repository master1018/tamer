public class VerboseGC {
    private MBeanServerConnection server;
    private JMXConnector jmxc;
    public VerboseGC(String hostname, int port) {
        System.out.println("Connecting to " + hostname + ":" + port);
        String urlPath = "/jndi/rmi:
        connect(urlPath);
   }
   public void dump(long interval, long samples) {
        try {
            PrintGCStat pstat = new PrintGCStat(server);
            for (int i = 0; i < samples; i++) {
                pstat.printVerboseGc();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
    }
    private void connect(String urlPath) {
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
            this.jmxc = JMXConnectorFactory.connect(url);
            this.server = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
    }
    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
        }
        String hostname = "";
        int port = -1;
        long interval = 5000; 
        long mins = 5;
        for (String arg: args) {
            if (arg.startsWith("-")) {
                if (arg.equals("-h") ||
                    arg.equals("-help") ||
                    arg.equals("-?")) {
                    usage();
                } else if (arg.startsWith("-interval=")) {
                    try {
                        interval = Integer.parseInt(arg.substring(10)) * 1000;
                    } catch (NumberFormatException ex) {
                        usage();
                    }
                } else if (arg.startsWith("-duration=")) {
                    try {
                        mins = Integer.parseInt(arg.substring(10));
                    } catch (NumberFormatException ex) {
                        usage();
                    }
                } else {
                    System.err.println("Unrecognized option: " + arg);
                    usage();
                }
            } else {
                String[] arg2 = arg.split(":");
                if (arg2.length != 2) {
                    usage();
                }
                hostname = arg2[0];
                try {
                    port = Integer.parseInt(arg2[1]);
                } catch (NumberFormatException x) {
                    usage();
                }
                if (port < 0) {
                    usage();
                }
            }
        }
        VerboseGC vgc = new VerboseGC(hostname, port);
        long samples = (mins * 60 * 1000) / interval;
        vgc.dump(interval, samples);
    }
    private static void usage() {
        System.out.print("Usage: java VerboseGC <hostname>:<port> ");
        System.out.println(" [-interval=seconds] [-duration=minutes]");
    }
}

public class SetMBeanServerForwarder {
    static final boolean optionalFlag;
    static {
        Class genericClass = null;
        try {
            genericClass =
            Class.forName("javax.management.remote.generic.GenericConnector");
        } catch (ClassNotFoundException x) {
        }
        optionalFlag = (genericClass != null);
    }
    final static String[] mandatoryList = {
        "service:jmx:rmi:
    };
    final static String[] optionalList = {
        "service:jmx:jmxmp:
    };
    public static int test(String[] urls) {
        int errorCount = 0;
        for (int i=0;i<urls.length;i++) {
            try {
                final MBeanServer mbs = MBeanServerFactory.newMBeanServer();
                final JMXConnectorServer cs1,cs2;
                final JMXServiceURL      url;
                System.out.println("*** -----------------------------------");
                System.out.println("*** JMXConnectorServer("+urls[i]+")");
                System.out.println("*** -----------------------------------");
                try {
                    url = new JMXServiceURL(urls[i]);
                    cs1 = JMXConnectorServerFactory
                        .newJMXConnectorServer(url,(Map)null,mbs);
                    cs2 = JMXConnectorServerFactory
                        .newJMXConnectorServer(url,(Map)null,null);
                } catch (Throwable thr) {
                    System.out.println("Failed to create ConnectorServer "+
                                       "from [" + urls[i] +"]: " + thr);
                    thr.printStackTrace();
                    errorCount++;
                    continue;
                }
                try {
                    cs1.setMBeanServerForwarder(null);
                    errorCount++;
                    System.out.println("Expected IllegalArgumentException "+
                                       " not thrown (null forwarder) for " +
                                       url);
                    System.out.println("\t\t[connected to MBeanServer]");
                } catch (IllegalArgumentException iae) {
                    System.out.println("Received expected exception: " +
                                       iae);
                }
                try {
                    final MBeanServerForwarder fwd = new
                        MBeanServerAccessController() {
                            protected void checkRead() {}
                            protected void checkWrite() {}
                        };
                    cs1.setMBeanServerForwarder(fwd);
                    if (cs1.getMBeanServer() != fwd) {
                        System.out.println("MBeanServerForwarder not set "+
                                           "for " + url);
                        System.out.println("\t\t[connected to MBeanServer]");
                        throw new AssertionError("cs1.getMBeanServer()!=fwd");
                    }
                    if (fwd.getMBeanServer() != mbs) {
                        System.out.println("MBeanServer not set in Forwarder"+
                                           " for " + url);
                        System.out.println("\t\t[connected to MBeanServer]");
                        throw new AssertionError("fwd.getMBeanServer()!=mbs");
                    }
                    System.out.println("MBeanServerForwarder successfully "+
                                       "set for " + url);
                    System.out.println("\t\t[connected to MBeanServer]");
                } catch (Throwable x) {
                    errorCount++;
                    System.out.println("Failed to set forwarder for " +
                                       url);
                    System.out.println("\t\t[connected to MBeanServer]");
                    System.out.println("Unexpected exception: " +
                                       x);
                    x.printStackTrace();
                }
                try {
                    cs2.setMBeanServerForwarder(null);
                    errorCount++;
                    System.out.println("Expected IllegalArgumentException "+
                                       " not thrown (null forwarder) for " +
                                       url);
                    System.out.println("\t\t[not connected to MBeanServer]");
                } catch (IllegalArgumentException iae) {
                    System.out.println("Received expected exception: " +
                                       iae);
                }
                try {
                    final MBeanServerForwarder fwd = new
                        MBeanServerAccessController() {
                            protected void checkRead() {}
                            protected void checkWrite() {}
                        };
                    cs2.setMBeanServerForwarder(fwd);
                    if (cs2.getMBeanServer() != fwd) {
                        System.out.println("MBeanServerForwarder not set "+
                                           "for " + url);
                        System.out.println("\t\t[not connected to MBeanServer]");
                        throw new AssertionError("cs2.getMBeanServer()!=fwd");
                    }
                    final ObjectName name =
                        new ObjectName(":type="+cs2.getClass().getName()+
                                       ",url="+ObjectName.quote(urls[i]));
                    mbs.registerMBean(cs2,name);
                    try {
                        if (cs2.getMBeanServer() != fwd) {
                            System.out.
                                println("MBeanServerForwarder changed "+
                                        "for " + url);
                            System.out.
                                println("\t\t[registerMBean]");
                            throw new
                                AssertionError("cs2.getMBeanServer()!=fwd");
                        }
                        if (fwd.getMBeanServer() != null) {
                            System.out.
                                println("MBeanServer changed in Forwarder"+
                                        " for " + url);
                            System.out.println("\t\t[registerMBean]");
                            throw new
                                AssertionError("fwd.getMBeanServer()!=null");
                        }
                    } finally {
                        mbs.unregisterMBean(name);
                    }
                    System.out.println("MBeanServerForwarder successfully "+
                                       "set for " + url);
                    System.out.println("\t\t[not connected to MBeanServer]");
                } catch (Throwable x) {
                    errorCount++;
                    System.out.println("Failed to set forwarder for " +
                                       url);
                    System.out.println("\t\t[not connected to MBeanServer]");
                    System.out.println("Unexpected exception: " +
                                       x);
                    x.printStackTrace();
                }
            } catch (Exception x) {
                System.err.println("Unexpected exception for " +
                                   urls[i] + ": " + x);
                x.printStackTrace();
                errorCount++;
            }
        }
        return errorCount;
    }
    public static void main(String args[]) {
        int errCount = 0;
        errCount += test(mandatoryList);
        if (optionalFlag) errCount += test(optionalList);
        if (errCount > 0) {
            System.err.println("SetMBeanServerForwarder failed: " +
                               errCount + " error(s) reported.");
            System.exit(1);
        }
        System.out.println("SetMBeanServerForwarder passed.");
    }
}

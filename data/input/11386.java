public class MapNullValuesTest {
    private static int port;
    private Map map0;
    private Map map1;
    private Map map2;
    private Map map3;
    private Map maps[];
    public MapNullValuesTest() {
        map0 = new HashMap();
        map1 = new HashMap();
        map1.put("key1", "value1");
        map1.put("key2", "value2");
        map1.put("key3", "value3");
        map2 = new HashMap();
        map2.put("key1", "value1");
        map2.put("key2", null);
        map2.put("key3", "value3");
        map2.put("key4", null);
        map2.put("key5", "value5");
        map3 = new HashMap();
        map3.put("key1", "value1");
        map3.put(null, "value2");
        map3.put("key3", "value3");
        maps = new Map[] { map0, map1, map2, map3 };
    }
    private void checkContents(Map m, Hashtable t)
        throws IllegalArgumentException {
        int size = m.size();
        Set s = m.entrySet();
        for (Iterator i = s.iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            if (key == null || value == null) { 
                size--;
            } else { 
                if (t.get(key) == null)
                    throw new IllegalArgumentException("Unknown key!");
                else if (!t.get(key).equals(value))
                    throw new IllegalArgumentException("Value mismatch!");
            }
        }
        if (t.size() != size)
            throw new IllegalArgumentException("Size mismatch!");
    }
    private int mapToHashtableTests() {
        int errorCount = 0;
        echo("");
        echo(dashedMessage("Run MapToHashtable Tests"));
        for (int i = 0; i < maps.length; i++) {
            echo("\n>>> MapToHashtable Test [" + i + "]");
            try {
                echo("\tMap = " + maps[i]);
                Hashtable t = EnvHelp.mapToHashtable(maps[i]);
                echo("\tHashtable = " + t);
                checkContents(maps[i], t);
                echo("\tTest [" + i + "] PASSED!");
            } catch (Exception e) {
                errorCount++;
                echo("\tTest [" + i + "] FAILED!");
                e.printStackTrace(System.out);
            }
        }
        if (errorCount == 0) {
            echo("");
            echo(dashedMessage("MapToHashtable Tests PASSED!"));
        } else {
            echo("");
            echo(dashedMessage("MapToHashtable Tests FAILED!"));
        }
        return errorCount;
    }
    private int jmxConnectorServerFactoryTests() {
        int errorCount = 0;
        echo("");
        echo(dashedMessage("Run JMXConnectorServerFactory Tests"));
        for (int i = 0; i < maps.length - 1; i++) {
            echo("\n>>> JMXConnectorServerFactory Test [" + i + "]");
            try {
                echo("\tMap = " + maps[i]);
                echo("\tCreate the MBean server");
                MBeanServer mbs = MBeanServerFactory.createMBeanServer();
                echo("\tCreate the RMI connector server");
                JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi:
                                      port + "/JMXConnectorServerFactory" + i);
                JMXConnectorServer jmxcs =
                    JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                    maps[i],
                                                                    mbs);
                echo("\tStart the RMI connector server");
                jmxcs.start();
                echo("\tCall RMIConnectorServer.toJMXConnector(Map)");
                jmxcs.toJMXConnector(maps[i]);
                echo("\tStop the RMI connector server");
                jmxcs.stop();
                echo("\tTest [" + i + "] PASSED!");
            } catch (Exception e) {
                errorCount++;
                echo("\tTest [" + i + "] FAILED!");
                e.printStackTrace(System.out);
            }
        }
        if (errorCount == 0) {
            echo("");
            echo(dashedMessage("JMXConnectorServerFactory Tests PASSED!"));
        } else {
            echo("");
            echo(dashedMessage("JMXConnectorServerFactory Tests FAILED!"));
        }
        return errorCount;
    }
    private int jmxConnectorFactoryTests() {
        int errorCount = 0;
        echo("");
        echo(dashedMessage("Run JMXConnectorFactory Tests"));
        for (int i = 0; i < maps.length - 1; i++) {
            echo("\n>>> JMXConnectorFactory Test [" + i + "]");
            try {
                echo("\tMap = " + maps[i]);
                echo("\tCreate the MBean server");
                MBeanServer mbs = MBeanServerFactory.createMBeanServer();
                echo("\tCreate the RMI connector server");
                JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi:
                                      port + "/JMXConnectorFactory" + i);
                JMXConnectorServer jmxcs =
                    JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                    null,
                                                                    mbs);
                echo("\tStart the RMI connector server");
                jmxcs.start();
                echo("\tCreate and connect the RMI connector");
                JMXConnector jmxc =
                    JMXConnectorFactory.connect(jmxcs.getAddress(), maps[i]);
                echo("\tClose the RMI connector");
                jmxc.close();
                echo("\tTest [" + i + "] PASSED!");
            } catch (Exception e) {
                errorCount++;
                echo("\tTest [" + i + "] FAILED!");
                e.printStackTrace(System.out);
            }
        }
        if (errorCount == 0) {
            echo("");
            echo(dashedMessage("JMXConnectorFactory Tests PASSED!"));
        } else {
            echo("");
            echo(dashedMessage("JMXConnectorFactory Tests FAILED!"));
        }
        return errorCount;
    }
    private int nullKeyFactoryTests() {
        int errorCount = 0;
        echo("");
        echo(dashedMessage("Run Null Key Factory Tests"));
        echo("\tMap = " + map3);
        try {
            String urlStr =
                "service:jmx:rmi:
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            JMXServiceURL url = null;
            JMXConnectorServer jmxcs = null;
            JMXConnector jmxc = null;
            echo("\tJMXConnectorServerFactory.newJMXConnectorServer()");
            try {
                url = new JMXServiceURL(urlStr + "1");
                jmxcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                        map3,
                                                                        mbs);
                errorCount++;
                echo("\tTest FAILED!");
            } catch (Exception e) {
                echo("\tException Message: " + e.getMessage());
                echo("\tTest PASSED!");
            }
            echo("\tJMXConnectorServerFactory.toJMXConnector()");
            try {
                url = new JMXServiceURL(urlStr + "2");
                jmxcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                        null,
                                                                        mbs);
                jmxcs.start();
                jmxcs.toJMXConnector(map3);
                errorCount++;
                echo("\tTest FAILED!");
            } catch (Exception e) {
                echo("\tException Message: " + e.getMessage());
                echo("\tTest PASSED!");
            } finally {
                jmxcs.stop();
            }
            echo("\tJMXConnectorFactory.newJMXConnector()");
            try {
                url = new JMXServiceURL(urlStr + "3");
                jmxcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                        null,
                                                                        mbs);
                jmxcs.start();
                jmxc = JMXConnectorFactory.newJMXConnector(jmxcs.getAddress(),
                                                           map3);
                errorCount++;
                echo("\tTest FAILED!");
            } catch (Exception e) {
                echo("\tException Message: " + e.getMessage());
                echo("\tTest PASSED!");
            } finally {
                jmxcs.stop();
            }
            echo("\tJMXConnectorFactory.connect()");
            try {
                url = new JMXServiceURL(urlStr + "4");
                jmxcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                        null,
                                                                        mbs);
                jmxcs.start();
                jmxc = JMXConnectorFactory.connect(jmxcs.getAddress(), map3);
                errorCount++;
                echo("\tTest FAILED!");
            } catch (Exception e) {
                echo("\tException Message: " + e.getMessage());
                echo("\tTest PASSED!");
            } finally {
                jmxcs.stop();
            }
            echo("\tJMXConnector.connect()");
            try {
                url = new JMXServiceURL(urlStr + "5");
                jmxcs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                        null,
                                                                        mbs);
                jmxcs.start();
                jmxc = JMXConnectorFactory.newJMXConnector(jmxcs.getAddress(),
                                                           null);
                jmxc.connect(map3);
                errorCount++;
                echo("\tTest FAILED!");
            } catch (Exception e) {
                echo("\tException Message: " + e.getMessage());
                echo("\tTest PASSED!");
            } finally {
                jmxcs.stop();
            }
        } catch (Exception e) {
            echo("\tGot unexpected exception!");
            e.printStackTrace(System.out);
            errorCount = 1;
        }
        if (errorCount == 0) {
            echo("");
            echo(dashedMessage("Null Key Factory Tests PASSED!"));
        } else {
            echo("");
            echo(dashedMessage("Null Key Factory Tests FAILED!"));
        }
        return errorCount;
    }
    private static String dashedMessage(String message) {
        final int MAX_LINE = 80;
        StringBuffer sb = new StringBuffer(message);
        sb.append(" ");
        for (int i = MAX_LINE; i > message.length() + 1; i--)
            sb.append("-");
        return sb.toString();
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main(String[] args) throws Exception {
        int errorCount = 0;
        MapNullValuesTest test = new MapNullValuesTest();
        echo("");
        echo(dashedMessage("Start RMI registry"));
        Registry reg = null;
        port = 7500;
        while (port++ < 7550) {
            try {
                reg = LocateRegistry.createRegistry(port);
                echo("\nRMI registry running on port " + port);
                break;
            } catch (RemoteException e) {
                echo("\nFailed to create RMI registry on port " + port);
                e.printStackTrace(System.out);
            }
        }
        if (reg == null) {
            System.exit(1);
        }
        errorCount += test.mapToHashtableTests();
        errorCount += test.jmxConnectorServerFactoryTests();
        errorCount += test.jmxConnectorFactoryTests();
        errorCount += test.nullKeyFactoryTests();
        if (errorCount == 0) {
            echo("\nNull values for key/value pairs in Map Tests PASSED!");
        } else {
            echo("\nNull values for key/value pairs in Map Tests FAILED!");
            System.exit(1);
        }
    }
}

public class MissingClassTest {
    private static final int NNOTIFS = 50;
    private static ClassLoader clientLoader, serverLoader;
    private static Object serverUnknown;
    private static Exception clientUnknown;
    private static ObjectName on;
    private static final Object[] NO_OBJECTS = new Object[0];
    private static final String[] NO_STRINGS = new String[0];
    private static final Object unserializableObject = Thread.currentThread();
    public static void main(String[] args) throws Exception {
        System.out.println("Test that the client or server end of a " +
                           "connection does not fail if sent an object " +
                           "it cannot deserialize");
        on = new ObjectName("test:type=Test");
        ClassLoader testLoader = MissingClassTest.class.getClassLoader();
        clientLoader =
            new SingleClassLoader("$ServerUnknown$", HashMap.class,
                                  testLoader);
        serverLoader =
            new SingleClassLoader("$ClientUnknown$", Exception.class,
                                  testLoader);
        serverUnknown =
            clientLoader.loadClass("$ServerUnknown$").newInstance();
        clientUnknown = (Exception)
            serverLoader.loadClass("$ClientUnknown$").newInstance();
        final String[] protos = {"rmi",  "jmxmp"};
        boolean ok = true;
        for (int i = 0; i < protos.length; i++) {
            try {
                ok &= test(protos[i]);
            } catch (Exception e) {
                System.out.println("TEST FAILED WITH EXCEPTION:");
                e.printStackTrace(System.out);
                ok = false;
            }
        }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto) throws Exception {
        System.out.println("Testing for proto " + proto);
        boolean ok = true;
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        mbs.createMBean(Test.class.getName(), on);
        JMXConnectorServer cs;
        JMXServiceURL url = new JMXServiceURL(proto, null, 0);
        Map serverMap = new HashMap();
        serverMap.put(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER,
                      serverLoader);
        serverMap.put("jmx.remote.x.server.connection.timeout", "888888888");
        try {
            cs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                 serverMap,
                                                                 mbs);
        } catch (MalformedURLException e) {
            System.out.println("System does not recognize URL: " + url +
                               "; ignoring");
            return true;
        }
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        Map clientMap = new HashMap();
        clientMap.put(JMXConnectorFactory.DEFAULT_CLASS_LOADER,
                      clientLoader);
        System.out.println("Connecting for client-unknown test");
        JMXConnector client = JMXConnectorFactory.connect(addr, clientMap);
        CNListener cnListener = new CNListener();
        client.addConnectionNotificationListener(cnListener, null, null);
        MBeanServerConnection mbsc = client.getMBeanServerConnection();
        System.out.println("Getting attribute with class unknown to client");
        try {
            Object result = mbsc.getAttribute(on, "ClientUnknown");
            System.out.println("TEST FAILS: getAttribute for class " +
                               "unknown to client should fail, returned: " +
                               result);
            ok = false;
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MARSHAL)  
                cause = cause.getCause();
            if (cause instanceof ClassNotFoundException) {
                System.out.println("Success: got an IOException wrapping " +
                                   "a ClassNotFoundException");
            } else {
                System.out.println("TEST FAILS: Caught IOException (" + e +
                                   ") but cause should be " +
                                   "ClassNotFoundException: " + cause);
                ok = false;
            }
        }
        System.out.println("Doing queryNames to ensure connection alive");
        Set names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        System.out.println("Provoke exception of unknown class");
        try {
            mbsc.invoke(on, "throwClientUnknown", NO_OBJECTS, NO_STRINGS);
            System.out.println("TEST FAILS: did not get exception");
            ok = false;
        } catch (IOException e) {
            Throwable wrapped = e.getCause();
            if (wrapped instanceof MARSHAL)  
                wrapped = wrapped.getCause();
            if (wrapped instanceof ClassNotFoundException) {
                System.out.println("Success: got an IOException wrapping " +
                                   "a ClassNotFoundException: " +
                                   wrapped);
            } else {
                System.out.println("TEST FAILS: Got IOException but cause " +
                                   "should be ClassNotFoundException: ");
                if (wrapped == null)
                    System.out.println("(null)");
                else
                    wrapped.printStackTrace(System.out);
                ok = false;
            }
        } catch (Exception e) {
            System.out.println("TEST FAILS: Got wrong exception: " +
                               "should be IOException with cause " +
                               "ClassNotFoundException:");
            e.printStackTrace(System.out);
            ok = false;
        }
        System.out.println("Doing queryNames to ensure connection alive");
        names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        ok &= notifyTest(client, mbsc);
        System.out.println("Doing queryNames to ensure connection alive");
        names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        for (int i = 0; i < 2; i++) {
            boolean setAttribute = (i == 0); 
            String what = setAttribute ? "setAttribute" : "invoke";
            System.out.println("Trying " + what +
                               " with class unknown to server");
            try {
                if (setAttribute) {
                    mbsc.setAttribute(on, new Attribute("ServerUnknown",
                                                        serverUnknown));
                } else {
                    mbsc.invoke(on, "useServerUnknown",
                                new Object[] {serverUnknown},
                                new String[] {"java.lang.Object"});
                }
                System.out.println("TEST FAILS: " + what + " with " +
                                   "class unknown to server should fail " +
                                   "but did not");
                ok = false;
            } catch (IOException e) {
                Throwable cause = e.getCause();
                if (cause instanceof MARSHAL)  
                    cause = cause.getCause();
                if (cause instanceof ClassNotFoundException) {
                    System.out.println("Success: got an IOException " +
                                       "wrapping a ClassNotFoundException");
                } else {
                    System.out.println("TEST FAILS: Caught IOException (" + e +
                                       ") but cause should be " +
                                       "ClassNotFoundException: " + cause);
                    e.printStackTrace(System.out); 
                    ok = false;
                }
            }
        }
        System.out.println("Doing queryNames to ensure connection alive");
        names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        System.out.println("Trying to get unserializable attribute");
        try {
            mbsc.getAttribute(on, "Unserializable");
            System.out.println("TEST FAILS: get unserializable worked " +
                               "but should not");
            ok = false;
        } catch (IOException e) {
            System.out.println("Success: got an IOException: " + e +
                               " (cause: " + e.getCause() + ")");
        }
        System.out.println("Doing queryNames to ensure connection alive");
        names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        System.out.println("Trying to set unserializable attribute");
        try {
            Attribute attr =
                new Attribute("Unserializable", unserializableObject);
            mbsc.setAttribute(on, attr);
            System.out.println("TEST FAILS: set unserializable worked " +
                               "but should not");
            ok = false;
        } catch (IOException e) {
            System.out.println("Success: got an IOException: " + e +
                               " (cause: " + e.getCause() + ")");
        }
        System.out.println("Doing queryNames to ensure connection alive");
        names = mbsc.queryNames(null, null);
        System.out.println("queryNames returned " + names);
        System.out.println("Trying to throw unserializable exception");
        try {
            mbsc.invoke(on, "throwUnserializable", NO_OBJECTS, NO_STRINGS);
            System.out.println("TEST FAILS: throw unserializable worked " +
                               "but should not");
            ok = false;
        } catch (IOException e) {
            System.out.println("Success: got an IOException: " + e +
                               " (cause: " + e.getCause() + ")");
        }
        client.removeConnectionNotificationListener(cnListener);
        ok = ok && !cnListener.failed;
        client.close();
        cs.stop();
        if (ok)
            System.out.println("Test passed for protocol " + proto);
        System.out.println();
        return ok;
    }
    private static class TestListener implements NotificationListener {
        TestListener(LostListener ll) {
            this.lostListener = ll;
        }
        public void handleNotification(Notification n, Object h) {
            if (n instanceof JMXConnectionNotification
                && n.getType().equals(JMXConnectionNotification.NOTIFS_LOST)) {
                lostListener.handleNotification(n, h);
                return;
            }
            synchronized (result) {
                if (!n.getType().equals("interesting")
                    || !n.getUserData().equals("known")) {
                    System.out.println("TestListener received strange notif: "
                                       + notificationString(n));
                    result.failed = true;
                    result.notifyAll();
                } else {
                    result.knownCount++;
                    if (result.knownCount == NNOTIFS)
                        result.notifyAll();
                }
            }
        }
        private LostListener lostListener;
    }
    private static class LostListener implements NotificationListener {
        public void handleNotification(Notification n, Object h) {
            synchronized (result) {
                handle(n, h);
            }
        }
        private void handle(Notification n, Object h) {
            if (!(n instanceof JMXConnectionNotification)) {
                System.out.println("LostListener received strange notif: " +
                                   notificationString(n));
                result.failed = true;
                result.notifyAll();
                return;
            }
            JMXConnectionNotification jn = (JMXConnectionNotification) n;
            if (!jn.getType().equals(jn.NOTIFS_LOST)) {
                System.out.println("Ignoring JMXConnectionNotification: " +
                                   notificationString(jn));
                return;
            }
            final String msg = jn.getMessage();
            if ((!msg.startsWith("Dropped ")
                 || !msg.endsWith("classes were missing locally"))
                && (!msg.startsWith("Not serializable: "))) {
                System.out.println("Surprising NOTIFS_LOST getMessage: " +
                                   msg);
            }
            if (!(jn.getUserData() instanceof Long)) {
                System.out.println("JMXConnectionNotification userData " +
                                   "not a Long: " + jn.getUserData());
                result.failed = true;
            } else {
                int lost = ((Long) jn.getUserData()).intValue();
                result.lostCount += lost;
                if (result.lostCount == NNOTIFS*2)
                    result.notifyAll();
            }
        }
    }
    private static class TestFilter implements NotificationFilter {
        public boolean isNotificationEnabled(Notification n) {
            return (n.getType().equals("interesting"));
        }
    }
    private static class Result {
        int knownCount, lostCount;
        boolean failed;
    }
    private static Result result;
    private static boolean notifyTest(JMXConnector client,
                                      MBeanServerConnection mbsc)
            throws Exception {
        System.out.println("Send notifications including unknown ones");
        result = new Result();
        LostListener ll = new LostListener();
        client.addConnectionNotificationListener(ll, null, null);
        TestListener nl = new TestListener(ll);
        mbsc.addNotificationListener(on, nl, new TestFilter(), null);
        mbsc.invoke(on, "sendNotifs", NO_OBJECTS, NO_STRINGS);
        long deadline = System.currentTimeMillis() + 60000;
        long remain;
        while ((remain = deadline - System.currentTimeMillis()) >= 0) {
            synchronized (result) {
                if (result.failed
                    || (result.knownCount >= NNOTIFS
                        && result.lostCount >= NNOTIFS*2))
                    break;
                result.wait(remain);
            }
        }
        Thread.sleep(2);  
        if (result.failed) {
            System.out.println("TEST FAILS: Notification strangeness");
            return false;
        } else if (result.knownCount == NNOTIFS
                   && result.lostCount == NNOTIFS*2) {
            System.out.println("Success: received known notifications and " +
                               "got NOTIFS_LOST for unknown and " +
                               "unserializable ones");
            return true;
        } else if (result.knownCount >= NNOTIFS
                || result.lostCount >= NNOTIFS*2) {
            System.out.println("TEST FAILS: Received too many notifs: " +
                    "known=" + result.knownCount + "; lost=" + result.lostCount);
            return false;
        } else {
            System.out.println("TEST FAILS: Timed out without receiving " +
                               "all notifs: known=" + result.knownCount +
                               "; lost=" + result.lostCount);
            return false;
        }
    }
    public static interface TestMBean {
        public Object getClientUnknown() throws Exception;
        public void throwClientUnknown() throws Exception;
        public void setServerUnknown(Object o) throws Exception;
        public void useServerUnknown(Object o) throws Exception;
        public Object getUnserializable() throws Exception;
        public void setUnserializable(Object un) throws Exception;
        public void throwUnserializable() throws Exception;
        public void sendNotifs() throws Exception;
    }
    public static class Test extends NotificationBroadcasterSupport
            implements TestMBean {
        public Object getClientUnknown() {
            return clientUnknown;
        }
        public void throwClientUnknown() throws Exception {
            throw clientUnknown;
        }
        public void setServerUnknown(Object o) {
            throw new IllegalArgumentException("setServerUnknown succeeded "+
                                               "but should not have");
        }
        public void useServerUnknown(Object o) {
            throw new IllegalArgumentException("useServerUnknown succeeded "+
                                               "but should not have");
        }
        public Object getUnserializable() {
            return unserializableObject;
        }
        public void setUnserializable(Object un) {
            throw new IllegalArgumentException("setUnserializable succeeded " +
                                               "but should not have");
        }
        public void throwUnserializable() throws Exception {
            throw new Exception() {
                private Object unserializable = unserializableObject;
            };
        }
        public void sendNotifs() {
            Notification known =
                new Notification("interesting", this, 1L, 1L, "known");
            known.setUserData("known");
            Notification unknown =
                new Notification("interesting", this, 1L, 1L, "unknown");
            unknown.setUserData(clientUnknown);
            Notification boring =
                new Notification("boring", this, 1L, 1L, "boring");
            Notification tricky =
                new Notification("interesting", this, 1L, 1L, "tricky");
            tricky.setUserData(unserializableObject);
            try {
                new ObjectOutputStream(new ByteArrayOutputStream())
                    .writeObject(tricky);
                System.out.println("TEST INCORRECT: tricky notif is " +
                                   "serializable");
                System.exit(1);
            } catch (NotSerializableException e) {
            } catch (IOException e) {
                System.out.println("TEST INCORRECT: tricky notif " +
                                   "serialization check failed");
                System.exit(1);
            }
            long seed = System.currentTimeMillis();
            System.out.println("Random number seed is " + seed);
            Random r = new Random(seed);
            int knownCount = NNOTIFS;   
            int unknownCount = NNOTIFS; 
            int trickyCount = NNOTIFS;  
            int boringCount = NNOTIFS;  
            StringBuffer notifList = new StringBuffer();
            for (int i = NNOTIFS * 4; i > 0; i--) {
                int rand = r.nextInt(i);
                if ((rand -= knownCount) < 0) {
                    notifList.append('k');
                    knownCount--;
                } else if ((rand -= unknownCount) < 0) {
                    notifList.append('u');
                    unknownCount--;
                } else if ((rand -= trickyCount) < 0) {
                    notifList.append('t');
                    trickyCount--;
                } else {
                    notifList.append('b');
                    boringCount--;
                }
            }
            if (knownCount != 0 || unknownCount != 0
                || trickyCount != 0 || boringCount != 0) {
                System.out.println("TEST INCORRECT: Shuffle failed: " +
                                   "known=" + knownCount +" unknown=" +
                                   unknownCount + " tricky=" + trickyCount +
                                   " boring=" + boringCount +
                                   " deal=" + notifList);
                System.exit(1);
            }
            String notifs = notifList.toString();
            System.out.println("Shuffle: " + notifs);
            for (int i = 0; i < NNOTIFS * 4; i++) {
                Notification n;
                switch (notifs.charAt(i)) {
                case 'k': n = known; break;
                case 'u': n = unknown; break;
                case 't': n = tricky; break;
                case 'b': n = boring; break;
                default:
                    System.out.println("TEST INCORRECT: Bad shuffle char: " +
                                       notifs.charAt(i));
                    System.exit(1);
                    throw new Error();
                }
                sendNotification(n);
            }
        }
    }
    private static String notificationString(Notification n) {
        return n.getClass().getName() + "/" + n.getType() + " \"" +
            n.getMessage() + "\" <" + n.getUserData() + ">";
    }
    private static class CNListener implements NotificationListener {
        public void handleNotification(Notification n, Object o) {
            if (n instanceof JMXConnectionNotification) {
                JMXConnectionNotification jn = (JMXConnectionNotification)n;
                if (JMXConnectionNotification.FAILED.equals(jn.getType())) {
                    failed = true;
                }
            }
        }
        public boolean failed = false;
    }
}

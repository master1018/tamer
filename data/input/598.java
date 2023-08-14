public class GarbageCollectionNotificationTest {
    private static HashMap<String,Boolean> listenerInvoked = new HashMap<String,Boolean>();
    static volatile long count = 0;
    static volatile long number = 0;
    static Object synchronizer = new Object();
    static class GcListener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            String type = notif.getType();
            if (type.equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                GarbageCollectionNotificationInfo gcNotif =
                    GarbageCollectionNotificationInfo.from((CompositeData) notif.getUserData());
                String source = ((ObjectName)notif.getSource()).getCanonicalName();
                synchronized(synchronizer) {
                    if(!listenerInvoked.get(source)) {
                            listenerInvoked.put(((ObjectName)notif.getSource()).getCanonicalName(),true);
                            count++;
                            if(count >= number) {
                                synchronizer.notify();
                            }
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final Boolean isNotificationSupported = AccessController.doPrivileged (new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    try {
                        Class cl = Class.forName("sun.management.VMManagementImpl");
                        Field f = cl.getDeclaredField("gcNotificationSupport");
                        f.setAccessible(true);
                        return f.getBoolean(null);
                    } catch(ClassNotFoundException e) {
                        return false;
                    } catch(NoSuchFieldException e) {
                        return false;
                    } catch(IllegalAccessException e) {
                        return false;
                    }
                }
            });
        if(!isNotificationSupported) {
            System.out.println("GC Notification not supported by the JVM, test skipped");
            return;
        }
        final ObjectName gcMXBeanPattern =
                new ObjectName("java.lang:type=GarbageCollector,*");
        Set<ObjectName> names =
                mbs.queryNames(gcMXBeanPattern, null);
        if (names.isEmpty())
            throw new Exception("Test incorrect: no GC MXBeans");
        number = names.size();
        for (ObjectName n : names) {
            if(mbs.isInstanceOf(n,"javax.management.NotificationEmitter")) {
                listenerInvoked.put(n.getCanonicalName(),false);
                GcListener listener = new GcListener();
                mbs.addNotificationListener(n, listener, null, null);
            }
        }
        System.gc();
        Object data[] = new Object[32];
        for(int i = 0; i<100000000; i++) {
            data[i%32] = new int[8];
        }
        int wakeup = 0;
        synchronized(synchronizer) {
            while(count != number) {
                synchronizer.wait(10000);
                wakeup++;
                if(wakeup > 10)
                    break;
            }
        }
        for (String source : listenerInvoked.keySet()) {
            if(!listenerInvoked.get(source))
                throw new Exception("Test incorrect: notifications have not been sent for "
                                    + source);
        }
        System.out.println("Test passed");
    }
}

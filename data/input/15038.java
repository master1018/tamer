public class LowMemoryTest2 {
    private static volatile boolean listenerInvoked = false;
    private static String INDENT = "    ";
    static class SensorListener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            String type = notif.getType();
            if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED) ||
                type.equals(MemoryNotificationInfo.
                    MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
                listenerInvoked = true;
                MemoryNotificationInfo minfo = MemoryNotificationInfo.
                    from((CompositeData) notif.getUserData());
                System.out.print("Notification for " + minfo.getPoolName());
                System.out.print(" [type = " + type);
                System.out.println(" count = " + minfo.getCount() + "]");
                System.out.println(INDENT + "usage = " + minfo.getUsage());
            }
        }
    }
    static class BoundlessLoaderThread extends ClassLoader implements Runnable {
        static int count = 100000;
        Class loadNext() throws ClassNotFoundException {
            int begin[] = {
                0xca, 0xfe, 0xba, 0xbe, 0x00, 0x00, 0x00, 0x30,
                0x00, 0x0a, 0x0a, 0x00, 0x03, 0x00, 0x07, 0x07,
                0x00, 0x08, 0x07, 0x00, 0x09, 0x01, 0x00, 0x06,
                0x3c, 0x69, 0x6e, 0x69, 0x74, 0x3e, 0x01, 0x00,
                0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43,
                0x6f, 0x64, 0x65, 0x0c, 0x00, 0x04, 0x00, 0x05,
                0x01, 0x00, 0x0a, 0x54, 0x65, 0x73, 0x74 };
            int end [] = {
                0x01, 0x00, 0x10,
                0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e,
                0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63, 0x74,
                0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x04,
                0x00, 0x05, 0x00, 0x01, 0x00, 0x06, 0x00, 0x00,
                0x00, 0x11, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
                0x00, 0x05, 0x2a, 0xb7, 0x00, 0x01, 0xb1, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00 };
            String name = "Test" + Integer.toString(count++);
            byte value[];
            try {
                value = name.substring(4).getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException x) {
                throw new Error();
            }
            int len = begin.length + value.length + end.length;
            byte b[] = new byte[len];
            int i, pos=0;
            for (i=0; i<begin.length; i++) {
                b[pos++] = (byte)begin[i];
            }
            for (i=0; i<value.length; i++) {
                b[pos++] = value[i];
            }
            for (i=0; i<end.length; i++) {
                b[pos++] = (byte)end[i];
            }
            return defineClass(name, b, 0, b.length);
        }
        public void run() {
            List pools = ManagementFactory.getMemoryPoolMXBeans();
            boolean thresholdExceeded = false;
            for (;;) {
                try {
                    for (int i=0; i<10; i++) {
                        loadNext();
                    }
                } catch (ClassNotFoundException x) {
                    return;
                }
                if (listenerInvoked) {
                    return;
                }
                if (thresholdExceeded) {
                    try {
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException x) { }
                } else {
                    ListIterator i = pools.listIterator();
                    while (i.hasNext()) {
                        MemoryPoolMXBean p = (MemoryPoolMXBean) i.next();
                        if (p.getType() == MemoryType.NON_HEAP &&
                            p.isUsageThresholdSupported())
                        {
                            thresholdExceeded = p.isUsageThresholdExceeded();
                        }
                    }
                }
            }
        }
    }
    public static void main(String args[]) {
        ListIterator iter = ManagementFactory.getMemoryPoolMXBeans().listIterator();
        while (iter.hasNext()) {
            MemoryPoolMXBean p = (MemoryPoolMXBean) iter.next();
            if (p.getType() == MemoryType.NON_HEAP && p.isUsageThresholdSupported()) {
                MemoryUsage mu = p.getUsage();
                long threshold = (mu.getMax() * 80) / 100;
                p.setUsageThreshold(threshold);
                System.out.println("Selected memory pool for low memory " +
                        "detection.");
                MemoryUtil.printMemoryPool(p);
            }
        }
        MemoryMXBean mm = ManagementFactory.getMemoryMXBean();
        SensorListener l2 = new SensorListener();
        NotificationEmitter emitter = (NotificationEmitter) mm;
        emitter.addNotificationListener(l2, null, null);
        Thread thr = new Thread(new BoundlessLoaderThread());
        thr.start();
        try {
            thr.join();
        } catch (InterruptedException x) {
            throw new RuntimeException(x);
        }
        if (listenerInvoked) {
            System.out.println("Notification received - test passed.");
        } else {
            throw new RuntimeException("Test failed - notification not received!");
        }
    }
}

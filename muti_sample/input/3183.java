public class Test6581734 {
    private String poolName = "CMS";
    private String collectorName = "ConcurrentMarkSweep";
    public static void main(String [] args) {
        Test6581734 t = null;
        if (args.length==2) {
            t = new Test6581734(args[0], args[1]);
        } else {
            System.out.println("Defaulting to monitor CMS pool and collector.");
            t = new Test6581734();
        }
        t.run();
    }
    public Test6581734(String pool, String collector) {
        poolName = pool;
        collectorName = collector;
    }
    public Test6581734() {
    }
    public void run() {
        allocationWork(300*1024*1024);
        System.out.println("Done allocationWork");
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        int poolsFound = 0;
        int poolsWithStats = 0;
        for (int i=0; i<pools.size(); i++) {
            MemoryPoolMXBean pool = pools.get(i);
            String name = pool.getName();
            System.out.println("found pool: " + name);
            if (name.contains(poolName)) {
                long usage = pool.getCollectionUsage().getUsed();
                System.out.println(name + ": usage after GC = " + usage);
                poolsFound++;
                if (usage > 0) {
                    poolsWithStats++;
                }
            }
        }
        if (poolsFound == 0) {
            throw new RuntimeException("No matching memory pools found: test with -XX:+UseConcMarkSweepGC");
        }
        List<GarbageCollectorMXBean> collectors = ManagementFactory.getGarbageCollectorMXBeans();
        int collectorsFound = 0;
        int collectorsWithTime= 0;
        for (int i=0; i<collectors.size(); i++) {
            GarbageCollectorMXBean collector = collectors.get(i);
            String name = collector.getName();
            System.out.println("found collector: " + name);
            if (name.contains(collectorName)) {
                collectorsFound++;
                System.out.println(name + ": collection count = "
                                   + collector.getCollectionCount());
                System.out.println(name + ": collection time  = "
                                   + collector.getCollectionTime());
                if (collector.getCollectionCount() <= 0) {
                    throw new RuntimeException("collection count <= 0");
                }
                if (collector.getCollectionTime() > 0) {
                    collectorsWithTime++;
                }
            }
        }
        if (poolsWithStats < poolsFound) {
            throw new RuntimeException("pools found with zero stats");
        }
        if (collectorsWithTime<collectorsFound) {
            throw new RuntimeException("collectors found with zero time");
        }
        System.out.println("Test passed.");
    }
    public void allocationWork(long target) {
        long sizeAllocated = 0;
        List list = new LinkedList();
        long delay = 50;
        long count = 0;
        while (sizeAllocated < target) {
            int size = 1024*1024;
            byte [] alloc = new byte[size];
            if (count % 2 == 0) {
                list.add(alloc);
                sizeAllocated+=size;
                System.out.print(".");
            }
            try { Thread.sleep(delay); } catch (InterruptedException ie) { }
            count++;
        }
    }
}

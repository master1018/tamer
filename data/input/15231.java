public class DistinctSeeds {
    public static void main(String[] args) throws Exception {
        if (new Random().nextLong() == new Random().nextLong() ||
            new Random().nextLong() == new Random().nextLong())
            throw new RuntimeException("Random() seeds not unique.");
        class RandomCollector implements Runnable {
            long[] randoms = new long[1<<17];
            public void run() {
                for (int i = 0; i < randoms.length; i++)
                    randoms[i] = new Random().nextLong();
            }
        }
        final int threadCount = 2;
        List<RandomCollector> collectors = new ArrayList<>();
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            RandomCollector r = new RandomCollector();
            collectors.add(r);
            threads.add(new Thread(r));
        }
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads)
            thread.join();
        int collisions = 0;
        HashSet<Long> s = new HashSet<Long>();
        for (RandomCollector r : collectors) {
            for (long x : r.randoms) {
                if (s.contains(x))
                    collisions++;
                s.add(x);
            }
        }
        System.out.printf("collisions=%d%n", collisions);
        if (collisions > 10)
            throw new Error("too many collisions");
    }
}

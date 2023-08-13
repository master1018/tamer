public class MapLoops {
    static int nkeys       = 10000;
    static int pinsert     = 60;
    static int premove     = 2;
    static int maxThreads  = 100;
    static int nops        = 100000;
    static int removesPerMaxRandom;
    static int insertsPerMaxRandom;
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final List<Throwable> throwables
        = new CopyOnWriteArrayList<Throwable>();
    public static void main(String[] args) throws Exception {
        Class mapClass = null;
        if (args.length > 0) {
            try {
                mapClass = Class.forName(args[0]);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class " + args[0] + " not found.");
            }
        }
        else
            mapClass = java.util.concurrent.ConcurrentHashMap.class;
        if (args.length > 1)
            maxThreads = Integer.parseInt(args[1]);
        if (args.length > 2)
            nkeys = Integer.parseInt(args[2]);
        if (args.length > 3)
            pinsert = Integer.parseInt(args[3]);
        if (args.length > 4)
            premove = Integer.parseInt(args[4]);
        if (args.length > 5)
            nops = Integer.parseInt(args[5]);
        removesPerMaxRandom = (int)(((double)premove/100.0 * 0x7FFFFFFFL));
        insertsPerMaxRandom = (int)(((double)pinsert/100.0 * 0x7FFFFFFFL));
        System.out.print("Class: " + mapClass.getName());
        System.out.print(" threads: " + maxThreads);
        System.out.print(" size: " + nkeys);
        System.out.print(" ins: " + pinsert);
        System.out.print(" rem: " + premove);
        System.out.print(" ops: " + nops);
        System.out.println();
        int k = 1;
        int warmups = 2;
        for (int i = 1; i <= maxThreads;) {
            Thread.sleep(100);
            test(i, nkeys, mapClass);
            if (warmups > 0)
                --warmups;
            else if (i == k) {
                k = i << 1;
                i = i + (i >>> 1);
            }
            else if (i == 1 && k == 2) {
                i = k;
                warmups = 1;
            }
            else
                i = k;
        }
        pool.shutdown();
        if (! pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
            throw new Error();
        if (! throwables.isEmpty())
            throw new Error
                (throwables.size() + " thread(s) terminated abruptly.");
    }
    static Integer[] makeKeys(int n) {
        LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
        Integer[] key = new Integer[n];
        for (int i = 0; i < key.length; ++i)
            key[i] = new Integer(rng.next());
        return key;
    }
    static void shuffleKeys(Integer[] key) {
        Random rng = new Random();
        for (int i = key.length; i > 1; --i) {
            int j = rng.nextInt(i);
            Integer tmp = key[j];
            key[j] = key[i-1];
            key[i-1] = tmp;
        }
    }
    static void test(int i, int nkeys, Class mapClass) throws Exception {
        System.out.print("Threads: " + i + "\t:");
        Map<Integer, Integer> map = (Map<Integer,Integer>)mapClass.newInstance();
        Integer[] key = makeKeys(nkeys);
        LoopHelpers.BarrierTimer timer = new LoopHelpers.BarrierTimer();
        CyclicBarrier barrier = new CyclicBarrier(i+1, timer);
        for (int t = 0; t < i; ++t)
            pool.execute(new Runner(map, key, barrier));
        barrier.await();
        barrier.await();
        long time = timer.getTime();
        long tpo = time / (i * (long)nops);
        System.out.print(LoopHelpers.rightJustify(tpo) + " ns per op");
        double secs = (double)(time) / 1000000000.0;
        System.out.println("\t " + secs + "s run time");
        map.clear();
    }
    static class Runner implements Runnable {
        final Map<Integer,Integer> map;
        final Integer[] key;
        final LoopHelpers.SimpleRandom rng = new LoopHelpers.SimpleRandom();
        final CyclicBarrier barrier;
        int position;
        int total;
        Runner(Map<Integer,Integer> map, Integer[] key,  CyclicBarrier barrier) {
            this.map = map;
            this.key = key;
            this.barrier = barrier;
            position = key.length / 2;
        }
        int step() {
            int r = rng.next();
            position += (r & 7) - 3;
            while (position >= key.length) position -= key.length;
            while (position < 0) position += key.length;
            Integer k = key[position];
            Integer x = map.get(k);
            if (x != null) {
                if (x.intValue() != k.intValue())
                    throw new Error("bad mapping: " + x + " to " + k);
                if (r < removesPerMaxRandom) {
                    if (map.remove(k) != null) {
                        position = total % key.length; 
                        return 2;
                    }
                }
            }
            else if (r < insertsPerMaxRandom) {
                ++position;
                map.put(k, k);
                return 2;
            }
            total += r;
            return 1;
        }
        public void run() {
            try {
                barrier.await();
                int ops = nops;
                while (ops > 0)
                    ops -= step();
                barrier.await();
            }
            catch (Throwable throwable) {
                synchronized (System.err) {
                    System.err.println("--------------------------------");
                    throwable.printStackTrace();
                }
                throwables.add(throwable);
            }
        }
    }
}

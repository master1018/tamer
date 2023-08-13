public class Random implements Serializable {
    private static final long serialVersionUID = 3905348978240129619L;
    private static final long multiplier = 0x5deece66dL;
    private boolean haveNextNextGaussian;
    private long seed;
    private double nextNextGaussian;
    public Random() {
        setSeed(System.currentTimeMillis() + System.identityHashCode(this));
    }
    public Random(long seed) {
        setSeed(seed);
    }
    protected synchronized int next(int bits) {
        seed = (seed * multiplier + 0xbL) & ((1L << 48) - 1);
        return (int) (seed >>> (48 - bits));
    }
    public boolean nextBoolean() {
        return next(1) != 0;
    }
    public void nextBytes(byte[] buf) {
        int rand = 0, count = 0, loop = 0;
        while (count < buf.length) {
            if (loop == 0) {
                rand = nextInt();
                loop = 3;
            } else {
                loop--;
            }
            buf[count++] = (byte) rand;
            rand >>= 8;
        }
    }
    public double nextDouble() {
        return ((((long) next(26) << 27) + next(27)) / (double) (1L << 53));
    }
    public float nextFloat() {
        return (next(24) / 16777216f);
    }
    public synchronized double nextGaussian() {
        if (haveNextNextGaussian) { 
            haveNextNextGaussian = false;
            return nextNextGaussian;
        }
        double v1, v2, s;
        do {
            v1 = 2 * nextDouble() - 1; 
            v2 = 2 * nextDouble() - 1;
            s = v1 * v1 + v2 * v2;
        } while (s >= 1);
        double norm = Math.sqrt(-2 * Math.log(s) / s);
        nextNextGaussian = v2 * norm; 
        haveNextNextGaussian = true;
        return v1 * norm; 
    }
    public int nextInt() {
        return next(32);
    }
    public int nextInt(int n) {
        if (n > 0) {
            if ((n & -n) == n) {
                return (int) ((n * (long) next(31)) >> 31);
            }
            int bits, val;
            do {
                bits = next(31);
                val = bits % n;
            } while (bits - val + (n - 1) < 0);
            return val;
        }
        throw new IllegalArgumentException();
    }
    public long nextLong() {
        return ((long) next(32) << 32) + next(32);
    }
    public synchronized void setSeed(long seed) {
        this.seed = (seed ^ multiplier) & ((1L << 48) - 1);
        haveNextNextGaussian = false;
    }
}

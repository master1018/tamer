class Random implements java.io.Serializable {
    static final long serialVersionUID = 3905348978240129619L;
    private final AtomicLong seed;
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;
    public Random() {
        this(seedUniquifier() ^ System.nanoTime());
    }
    private static long seedUniquifier() {
        for (;;) {
            long current = seedUniquifier.get();
            long next = current * 181783497276652981L;
            if (seedUniquifier.compareAndSet(current, next))
                return next;
        }
    }
    private static final AtomicLong seedUniquifier
        = new AtomicLong(8682522807148012L);
    public Random(long seed) {
        this.seed = new AtomicLong(initialScramble(seed));
    }
    private static long initialScramble(long seed) {
        return (seed ^ multiplier) & mask;
    }
    synchronized public void setSeed(long seed) {
        this.seed.set(initialScramble(seed));
        haveNextNextGaussian = false;
    }
    protected int next(int bits) {
        long oldseed, nextseed;
        AtomicLong seed = this.seed;
        do {
            oldseed = seed.get();
            nextseed = (oldseed * multiplier + addend) & mask;
        } while (!seed.compareAndSet(oldseed, nextseed));
        return (int)(nextseed >>> (48 - bits));
    }
    public void nextBytes(byte[] bytes) {
        for (int i = 0, len = bytes.length; i < len; )
            for (int rnd = nextInt(),
                     n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE)
                bytes[i++] = (byte)rnd;
    }
    public int nextInt() {
        return next(32);
    }
    public int nextInt(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("n must be positive");
        if ((n & -n) == n)  
            return (int)((n * (long)next(31)) >> 31);
        int bits, val;
        do {
            bits = next(31);
            val = bits % n;
        } while (bits - val + (n-1) < 0);
        return val;
    }
    public long nextLong() {
        return ((long)(next(32)) << 32) + next(32);
    }
    public boolean nextBoolean() {
        return next(1) != 0;
    }
    public float nextFloat() {
        return next(24) / ((float)(1 << 24));
    }
    public double nextDouble() {
        return (((long)(next(26)) << 27) + next(27))
            / (double)(1L << 53);
    }
    private double nextNextGaussian;
    private boolean haveNextNextGaussian = false;
    synchronized public double nextGaussian() {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * nextDouble() - 1; 
                v2 = 2 * nextDouble() - 1; 
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s)/s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("seed", Long.TYPE),
        new ObjectStreamField("nextNextGaussian", Double.TYPE),
        new ObjectStreamField("haveNextNextGaussian", Boolean.TYPE)
    };
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        long seedVal = fields.get("seed", -1L);
        if (seedVal < 0)
          throw new java.io.StreamCorruptedException(
                              "Random: invalid seed");
        resetSeed(seedVal);
        nextNextGaussian = fields.get("nextNextGaussian", 0.0);
        haveNextNextGaussian = fields.get("haveNextNextGaussian", false);
    }
    synchronized private void writeObject(ObjectOutputStream s)
        throws IOException {
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("seed", seed.get());
        fields.put("nextNextGaussian", nextNextGaussian);
        fields.put("haveNextNextGaussian", haveNextNextGaussian);
        s.writeFields();
    }
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long seedOffset;
    static {
        try {
            seedOffset = unsafe.objectFieldOffset
                (Random.class.getDeclaredField("seed"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    private void resetSeed(long seedVal) {
        unsafe.putObjectVolatile(this, seedOffset, new AtomicLong(seedVal));
    }
}

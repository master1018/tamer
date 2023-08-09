public final class UID implements Serializable {
    private static int hostUnique;
    private static boolean hostUniqueSet = false;
    private static final Object lock = new Object();
    private static long lastTime = System.currentTimeMillis();
    private static short lastCount = Short.MIN_VALUE;
    private static final long serialVersionUID = 1086053664494604050L;
    private final int unique;
    private final long time;
    private final short count;
    public UID() {
        synchronized (lock) {
            if (!hostUniqueSet) {
                hostUnique = (new SecureRandom()).nextInt();
                hostUniqueSet = true;
            }
            unique = hostUnique;
            if (lastCount == Short.MAX_VALUE) {
                boolean interrupted = Thread.interrupted();
                boolean done = false;
                while (!done) {
                    long now = System.currentTimeMillis();
                    if (now <= lastTime) {
                        try {
                            Thread.currentThread().sleep(1);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    } else {
                        lastTime = now;
                        lastCount = Short.MIN_VALUE;
                        done = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            time = lastTime;
            count = lastCount++;
        }
    }
    public UID(short num) {
        unique = 0;
        time = 0;
        count = num;
    }
    private UID(int unique, long time, short count) {
        this.unique = unique;
        this.time = time;
        this.count = count;
    }
    public int hashCode() {
        return (int) time + (int) count;
    }
    public boolean equals(Object obj) {
        if (obj instanceof UID) {
            UID uid = (UID) obj;
            return (unique == uid.unique &&
                    count == uid.count &&
                    time == uid.time);
        } else {
            return false;
        }
    }
    public String toString() {
        return Integer.toString(unique,16) + ":" +
            Long.toString(time,16) + ":" +
            Integer.toString(count,16);
    }
    public void write(DataOutput out) throws IOException {
        out.writeInt(unique);
        out.writeLong(time);
        out.writeShort(count);
    }
    public static UID read(DataInput in) throws IOException {
        int unique = in.readInt();
        long time = in.readLong();
        short count = in.readShort();
        return new UID(unique, time, count);
    }
}

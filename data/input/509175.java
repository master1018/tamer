public final class UUID implements Serializable, Comparable<UUID> {
    private static final long serialVersionUID = -4856846361193249489L;
    private static SecureRandom rng;
    private long mostSigBits;
    private long leastSigBits;
    private transient int variant;
    private transient int version;
    private transient long timestamp;
    private transient int clockSequence;
    private transient long node;
    private transient int hash;
    public UUID(long mostSigBits, long leastSigBits) {
        super();
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
        init();
    }
    private void init() {
        int msbHash = (int) (mostSigBits ^ (mostSigBits >>> 32));
        int lsbHash = (int) (leastSigBits ^ (leastSigBits >>> 32));
        hash = msbHash ^ lsbHash;
        if ((leastSigBits & 0x8000000000000000L) == 0) {
            variant = 0;
        } else if ((leastSigBits & 0x4000000000000000L) != 0) {
            variant = (int) ((leastSigBits & 0xE000000000000000L) >>> 61);
        } else {
            variant = 2;
        }
        version = (int) ((mostSigBits & 0x000000000000F000) >>> 12);
        if (variant != 2 && version != 1) {
            return;
        }
        long timeLow = (mostSigBits & 0xFFFFFFFF00000000L) >>> 32;
        long timeMid = (mostSigBits & 0x00000000FFFF0000L) << 16;
        long timeHigh = (mostSigBits & 0x0000000000000FFFL) << 48;
        timestamp = timeLow | timeMid | timeHigh;
        clockSequence = (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
        node = (leastSigBits & 0x0000FFFFFFFFFFFFL);
    }
    public static UUID randomUUID() {
        byte[] data;
        synchronized (UUID.class) {
            if (rng == null) {
                rng = new SecureRandom();
            }
        }
        rng.nextBytes(data = new byte[16]);
        long msb = (data[0] & 0xFFL) << 56;
        msb |= (data[1] & 0xFFL) << 48;
        msb |= (data[2] & 0xFFL) << 40;
        msb |= (data[3] & 0xFFL) << 32;
        msb |= (data[4] & 0xFFL) << 24;
        msb |= (data[5] & 0xFFL) << 16;
        msb |= (data[6] & 0x0FL) << 8;
        msb |= (0x4L << 12); 
        msb |= (data[7] & 0xFFL);
        long lsb = (data[8] & 0x3FL) << 56;
        lsb |= (0x2L << 62); 
        lsb |= (data[9] & 0xFFL) << 48;
        lsb |= (data[10] & 0xFFL) << 40;
        lsb |= (data[11] & 0xFFL) << 32;
        lsb |= (data[12] & 0xFFL) << 24;
        lsb |= (data[13] & 0xFFL) << 16;
        lsb |= (data[14] & 0xFFL) << 8;
        lsb |= (data[15] & 0xFFL);
        return new UUID(msb, lsb);
    }
    public static UUID nameUUIDFromBytes(byte[] name) {
        if (name == null) {
            throw new NullPointerException();
        }
        byte[] hash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            hash = md.digest(name);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        long msb = (hash[0] & 0xFFL) << 56;
        msb |= (hash[1] & 0xFFL) << 48;
        msb |= (hash[2] & 0xFFL) << 40;
        msb |= (hash[3] & 0xFFL) << 32;
        msb |= (hash[4] & 0xFFL) << 24;
        msb |= (hash[5] & 0xFFL) << 16;
        msb |= (hash[6] & 0x0FL) << 8;
        msb |= (0x3L << 12); 
        msb |= (hash[7] & 0xFFL);
        long lsb = (hash[8] & 0x3FL) << 56;
        lsb |= (0x2L << 62); 
        lsb |= (hash[9] & 0xFFL) << 48;
        lsb |= (hash[10] & 0xFFL) << 40;
        lsb |= (hash[11] & 0xFFL) << 32;
        lsb |= (hash[12] & 0xFFL) << 24;
        lsb |= (hash[13] & 0xFFL) << 16;
        lsb |= (hash[14] & 0xFFL) << 8;
        lsb |= (hash[15] & 0xFFL);
        return new UUID(msb, lsb);
    }
    public static UUID fromString(String uuid) {
        if (uuid == null) {
            throw new NullPointerException();
        }
        int[] position = new int[5];
        int lastPosition = 1;
        int startPosition = 0;
        int i = 0;
        for (; i < position.length && lastPosition > 0; i++) {
            position[i] = uuid.indexOf("-", startPosition); 
            lastPosition = position[i];
            startPosition = position[i] + 1;
        }
        if (i != position.length || lastPosition != -1) {
            throw new IllegalArgumentException(Msg.getString("KA014") + uuid); 
        }
        long m1 = Long.parseLong(uuid.substring(0, position[0]), 16);
        long m2 = Long.parseLong(uuid.substring(position[0] + 1, position[1]),
                16);
        long m3 = Long.parseLong(uuid.substring(position[1] + 1, position[2]),
                16);
        long lsb1 = Long.parseLong(
                uuid.substring(position[2] + 1, position[3]), 16);
        long lsb2 = Long.parseLong(uuid.substring(position[3] + 1), 16);
        long msb = (m1 << 32) | (m2 << 16) | m3;
        long lsb = (lsb1 << 48) | lsb2;
        return new UUID(msb, lsb);
    }
    public long getLeastSignificantBits() {
        return leastSigBits;
    }
    public long getMostSignificantBits() {
        return mostSigBits;
    }
    public int version() {
        return version;
    }
    public int variant() {
        return variant;
    }
    public long timestamp() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return timestamp;
    }
    public int clockSequence() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return clockSequence;
    }
    public long node() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return node;
    }
    public int compareTo(UUID uuid) {
        if (uuid == this) {
            return 0;
        }
        if (this.mostSigBits != uuid.mostSigBits) {
            return this.mostSigBits < uuid.mostSigBits ? -1 : 1;
        }
        assert this.mostSigBits == uuid.mostSigBits;
        if (this.leastSigBits != uuid.leastSigBits) {
            return this.leastSigBits < uuid.leastSigBits ? -1 : 1;
        }
        assert this.leastSigBits == uuid.leastSigBits;
        return 0;
    }
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof UUID)) {
            return false;
        }
        UUID that = (UUID) object;
        return (this.leastSigBits == that.leastSigBits)
                && (this.mostSigBits == that.mostSigBits);
    }
    @Override
    public int hashCode() {
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(36);
        String msbStr = Long.toHexString(mostSigBits);
        if (msbStr.length() < 16) {
            int diff = 16 - msbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(msbStr);
        builder.insert(8, '-');
        builder.insert(13, '-');
        builder.append('-');
        String lsbStr = Long.toHexString(leastSigBits);
        if (lsbStr.length() < 16) {
            int diff = 16 - lsbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(lsbStr);
        builder.insert(23, '-');
        return builder.toString();
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        init();
    }
}

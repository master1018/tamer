public final class HashedVersion implements Comparable<HashedVersion> {
    public static HashedVersion of(long version, byte[] historyHash) {
        return new HashedVersion(version, historyHash);
    }
    public static HashedVersion unsigned(long version) {
        return new HashedVersion(version, new byte[0]);
    }
    private final long version;
    private final byte[] historyHash;
    private HashedVersion(long version, byte[] historyHash) {
        Preconditions.checkArgument(version >= 0, "negative version");
        Preconditions.checkNotNull(historyHash, "null history hash");
        this.version = version;
        this.historyHash = historyHash;
    }
    @Override
    public int compareTo(HashedVersion other) {
        return version != other.version ? Long.signum(version - other.version) : compare(historyHash, other.historyHash);
    }
    public long getVersion() {
        return version;
    }
    public byte[] getHistoryHash() {
        return historyHash;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Long.valueOf(version).hashCode();
        result = 31 * result + Arrays.hashCode(historyHash);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof HashedVersion)) {
            return false;
        } else {
            HashedVersion that = (HashedVersion) obj;
            return version == that.version && Arrays.equals(historyHash, that.historyHash);
        }
    }
    @Override
    public String toString() {
        return Long.toString(version) + ":" + CharBase64.encode(historyHash);
    }
    private static int compare(byte[] first, byte[] second) {
        if (first == second) {
            return 0;
        }
        for (int i = 0; i < first.length && i < second.length; i++) {
            if (first[i] != second[i]) {
                return Integer.signum(first[i] - second[i]);
            }
        }
        return Integer.signum(first.length - second.length);
    }
}

public class ChronoIndexData implements Comparable<ChronoIndexData> {
    private boolean chronoSort;
    private int index;
    private int revisionCounter;
    private long time;
    public ChronoIndexData(final long time, final int revisionCounter) {
        this.time = time;
        this.revisionCounter = revisionCounter;
        this.chronoSort = true;
    }
    public int compareTo(final ChronoIndexData info) {
        long value;
        if (chronoSort) {
            value = this.time - info.time;
        } else {
            value = this.revisionCounter - info.revisionCounter;
        }
        if (value == 0) {
            return 0;
        } else if (value > 0) {
            return 1;
        } else {
            return -1;
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return (this != (ChronoIndexData) obj) ? false : true;
    }
    public int getIndex() {
        return this.index;
    }
    public int getRevisionCounter() {
        return revisionCounter;
    }
    public long getTime() {
        return time;
    }
    public void setIndex(final int index) {
        this.index = index;
    }
    public void setSortFlag(final boolean chronoSort) {
        this.chronoSort = chronoSort;
    }
}

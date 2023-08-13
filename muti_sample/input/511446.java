public class BidiRun {
    private final int start;
    private final int limit;
    private final int level;
    public BidiRun(int start, int limit, int level) {
        this.start = start;
        this.limit = limit;
        this.level = level;
    }
    public int getLevel() {
        return level;
    }
    public int getLimit() {
        return limit;
    }
    public int getStart() {
        return start;
    }
    @Override
    public boolean equals(Object o) {
        return o == null || o.getClass() != BidiRun.class ? false
                : this.start == ((BidiRun) o).start
                        && this.limit == ((BidiRun) o).limit
                        && this.level == ((BidiRun) o).level;
    }
}

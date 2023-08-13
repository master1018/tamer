public abstract class BasicLogEvent implements LogEvent {
    protected final String id;
    protected final double start;
    protected double end;
    protected Compilation compilation;
    BasicLogEvent(double start, String id) {
        this.start = start;
        this.end = start;
        this.id = id;
    }
    public double getStart() {
        return start;
    }
    public double getEnd() {
        return end;
    }
    public void setEnd(double end) {
        this.end = end;
    }
    public double getElapsedTime() {
        return ((int) ((getEnd() - getStart()) * 1000)) / 1000.0;
    }
    public String getId() {
        return id;
    }
    public Compilation getCompilation() {
        return compilation;
    }
    public void setCompilation(Compilation compilation) {
        this.compilation = compilation;
    }
    abstract public void print(PrintStream stream);
}

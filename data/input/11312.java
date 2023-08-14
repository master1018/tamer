public final class QueuedJobCount extends IntegerSyntax
    implements PrintServiceAttribute {
    private static final long serialVersionUID = 7499723077864047742L;
    public QueuedJobCount(int value) {
        super (value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
               object instanceof QueuedJobCount);
    }
    public final Class<? extends Attribute> getCategory() {
        return QueuedJobCount.class;
    }
    public final String getName() {
        return "queued-job-count";
    }
}

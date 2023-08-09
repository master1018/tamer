public class LessThan<T extends Comparable<T>> extends CompareTo<T> {
    private static final long serialVersionUID = 6004888476822043880L;
    public LessThan(Comparable<T> value) {
        super(value);
    }
    @Override
    protected String getName() {
        return "lt";
    }
    @Override
    protected boolean matchResult(int result) {
        return result < 0;
    }
}

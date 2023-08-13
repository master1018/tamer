public abstract class AccumulativeRunnable<T> implements Runnable {
    private List<T> arguments = null;
    protected abstract void run(List<T> args);
    public final void run() {
        run(flush());
    }
    @SafeVarargs
    public final synchronized void add(T... args) {
        boolean isSubmitted = true;
        if (arguments == null) {
            isSubmitted = false;
            arguments = new ArrayList<T>();
        }
        Collections.addAll(arguments, args);
        if (!isSubmitted) {
            submit();
        }
    }
    protected void submit() {
        SwingUtilities.invokeLater(this);
    }
    private final synchronized List<T> flush() {
        List<T> list = arguments;
        arguments = null;
        return list;
    }
}

public abstract class TestThread
    extends Thread
{
    Exception failure = null;
    String name;
    protected final PrintStream log;
    Thread main;
    TestThread(String name, PrintStream log) {
        super("TestThread-" + name);
        this.name = name;
        this.log = log;
        this.main = Thread.currentThread();
        setDaemon(true);
    }
    TestThread(String name) {
        this(name, System.err);
    }
    abstract void go() throws Exception;
    public void run() {
        try {
            go();
        } catch (Exception x) {
            failure = x;
            main.interrupt();
        }
    }
    int finish(long timeout) {
        try {
            join(timeout);
        } catch (InterruptedException x) { }
        if (isAlive() && (failure == null))
            failure = new Exception(name + ": Timed out");
        if (failure != null) {
            failure.printStackTrace(log);
            return 0;
        }
        return 1;
    }
    void finishAndThrow(long timeout) throws Exception {
        try {
            join(timeout);
        } catch (InterruptedException x) { }
        if (failure != null)
            failure = new Exception(name + " threw an exception",
                                    failure);
        if (isAlive() && (failure == null))
            failure = new Exception(name + " timed out");
        if (failure != null)
            throw failure;
    }
    public String toString() {
        return name;
    }
}

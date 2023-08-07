public abstract class Kernel {
    public static Kernel kernel = null;
    public Kernel() {
        Lib.assertTrue(kernel == null);
        kernel = this;
    }
    public abstract void initialize(String[] args);
    public abstract void selfTest();
    public abstract void run();
    public abstract void terminate();
}

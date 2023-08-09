public class Another {
    public Another() {
        System.out.println("Constructing another");
        try {
            System.loadLibrary("nonexistent");
        } catch (UnsatisfiedLinkError ule) {
            System.out.println("Got expected ULE");
        }
    }
}

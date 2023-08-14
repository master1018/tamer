public class UseNativeLib implements java.io.Serializable, UseNativeLibMBean {
    public native int getRandom();
    static {
        try {
            System.loadLibrary("genrandom");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

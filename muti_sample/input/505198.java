public class GLJNILib {
     static {
         System.loadLibrary("gljni");
     }
     public static native void init(int width, int height);
     public static native void step();
     public static native void changeBackground();
}

public class GLDualLib {
     static {
         System.loadLibrary("gldualjni");
     }
     public static native void init(int width, int height);
     public static native void step();
}

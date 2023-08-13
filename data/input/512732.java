public class JNI {
  public static Runnable EXIT_JVM = new Runnable() {
    public void run() {
      System.out.println("Could not load '" + libraryName + "'");
      System.out.println("java.library.path = " 
          + System.getProperty("java.library.path"));
      System.exit(1);
    }
  };
  public static Runnable THROW_ERROR = new Runnable() {
    public void run() {
      throw new UnsatisfiedLinkError("Could not load '" + libraryName + "'");
    }
  };
  private static Runnable failureCallback = EXIT_JVM;
  private static Object callbackLock = new Object();
  private static String libraryName = "clearsilver-jni";
  public static void loadLibrary() {
    try {
      System.loadLibrary(libraryName);
    } catch (UnsatisfiedLinkError e) {
      synchronized (callbackLock) {
        if (failureCallback != null) {
          failureCallback.run();
        }
      }
    }    
  }
  public static void setFailureCallback(Runnable failureCallback) {
    synchronized(callbackLock) {
      JNI.failureCallback = failureCallback;
    }
  }
  public static void setLibraryName(String libraryName) {
    JNI.libraryName = libraryName;
  }
}
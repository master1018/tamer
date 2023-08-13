public class TestWebData {
  public final static byte[] test1 = {
    (byte)0x3c, (byte)0x68, (byte)0x74, (byte)0x6d,
    (byte)0x6c, (byte)0x3e, (byte)0x0a, (byte)0x3c,
    (byte)0x62, (byte)0x6f, (byte)0x64, (byte)0x79,
    (byte)0x3e, (byte)0x0a, (byte)0x3c, (byte)0x68,
    (byte)0x31, (byte)0x3e, (byte)0x48, (byte)0x65,
    (byte)0x6c, (byte)0x6c, (byte)0x6f, (byte)0x20,
    (byte)0x57, (byte)0x6f, (byte)0x72, (byte)0x6c,
    (byte)0x64, (byte)0x21, (byte)0x3c, (byte)0x2f,
    (byte)0x68, (byte)0x31, (byte)0x3e, (byte)0x0a,
    (byte)0x3c, (byte)0x2f, (byte)0x62, (byte)0x6f,
    (byte)0x64, (byte)0x79, (byte)0x3e, (byte)0x0a,
    (byte)0x3c, (byte)0x2f, (byte)0x68, (byte)0x74,
    (byte)0x6d, (byte)0x6c, (byte)0x3e, (byte)0x0a
  };
  public final static byte[] test2 = {
    (byte)0x3c, (byte)0x68, (byte)0x74, (byte)0x6d,
    (byte)0x6c, (byte)0x3e, (byte)0x0a, (byte)0x3c,
    (byte)0x62, (byte)0x6f, (byte)0x64, (byte)0x79,
    (byte)0x3e, (byte)0x0a, (byte)0x3c, (byte)0x68,
    (byte)0x31, (byte)0x3e, (byte)0x48, (byte)0x65,
    (byte)0x6c, (byte)0x6c, (byte)0x6f, (byte)0x20,
    (byte)0x57, (byte)0x6f, (byte)0x72, (byte)0x6c,
    (byte)0x64, (byte)0x21, (byte)0x3c, (byte)0x2f,
    (byte)0x68, (byte)0x31, (byte)0x3e, (byte)0x0a,
    (byte)0x3c, (byte)0x2f, (byte)0x62, (byte)0x6f,
    (byte)0x64, (byte)0x79, (byte)0x3e, (byte)0x0a,
    (byte)0x3c, (byte)0x2f, (byte)0x68, (byte)0x74,
    (byte)0x6d, (byte)0x6c, (byte)0x3e, (byte)0x0a
  };
  public final static String postContent = "user=111";
  public final static byte[][] tests = {
    test1,
    test2
  };
  public static TestWebData[] testParams = {
    new TestWebData(52, 14000000, "test1", "text/html", false),
    new TestWebData(52, 14000002, "test2", "unknown/unknown", false)
  };
  public static String[] testServerResponse = {
    "Redirecting 301",
    "Redirecting 302",
    "Redirecting 303",
    "Redirecting 307"
  };
  public final static int REDIRECT_301 = 0;
  public final static int REDIRECT_302 = 1;
  public final static int REDIRECT_303 = 2;
  public final static int REDIRECT_307 = 3;
  TestWebData(int length, int lastModified, String name, String type, boolean isDir) {
    testLength = length;
    testLastModified = lastModified;
    testName = name;
    testType = type;
    testDir = isDir;
  }
  public int testLength;
  public int testLastModified;
  public String testName;
  public String testType;
  public boolean testDir;
}

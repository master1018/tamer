public class Support_TestWebData {
  public final static byte[] test1 = utfBytes();
  public final static byte[] test2 = newBinaryFile(8192);
  private static byte[] utfBytes() {
    try {
      return "<html>\n<body>\n<h1>Hello World!</h1>\n</body>\n</html>\n".getBytes("UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError();
    }
  }
  private static byte[] newBinaryFile(int byteCount) {
    byte[] result = new byte[byteCount];
    for (int i = 0; i < result.length; ++i) {
      result[i] = (byte) i;
    }
    return result;
  }
  public final static String postContent = "user=111";
  public final static byte[][] tests = {
    test1,
    test2
  };
  public static Support_TestWebData[] testParams = {
    new Support_TestWebData(test1.length, 14000000, "test1", "text/html", false, 0),
    new Support_TestWebData(test2.length, 14000002, "test2", "unknown/unknown", false,
            new Date().getTime() + 100000)
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
  Support_TestWebData(int length, int lastModified, String name, String type, boolean isDir, long expDate) {
    testLength = length;
    testLastModified = lastModified;
    testName = name;
    testType = type;
    testDir = isDir;
    testExp = expDate;
  }
  private Support_TestWebData(String path, String type) {
    File file = new File(path);
    testLength = file.length();
    testLastModified = file.lastModified();
    testName = file.getName();
    testType = type;
    testDir = file.isDirectory();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    FileInputStream in = null;
    try {
        in = new FileInputStream(file);
        while (in.available() > 0) {
            out.write(in.read());
        }
        in.close();
        out.flush();
        test0Data = out.toByteArray();
        out.close();
        test0DataAvailable = true;
        return;
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
        }
    }
  }
  public static void initDynamicTestWebData(String path, String type) {
      test0Params = new Support_TestWebData(path, type);
  }
  public long testLength;
  public long testLastModified;
  public String testName;
  public String testType;
  public long testExp;
  public boolean testDir;
  public static boolean test0DataAvailable = false;
  public static byte[] test0Data;
  public static Support_TestWebData test0Params;
}

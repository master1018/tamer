public abstract class DOMTestCase
    extends DOMTest {
  private DOMTestFramework framework;
  public DOMTestCase() {
    framework = null;
  }
  public DOMTestCase(DOMTestDocumentBuilderFactory factory) {
    super(factory);
    framework = null;
  }
  public static void doMain(Class testClass, String[] args) {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    try {
      Class runnerClass = loader.loadClass("org.w3c.domts.JUnitRunner");
      Constructor runnerFactory =
          runnerClass.getConstructor(new Class[] {Class.class});
      Object junitRun =
          runnerFactory.newInstance(new Object[] {testClass});
      Class argsClass = loader.loadClass("[Ljava.lang.String;");
      Method execMethod =
          runnerClass.getMethod("execute", new Class[] {argsClass});
      execMethod.invoke(junitRun, new Object[] {args});
    }
    catch (InvocationTargetException ex) {
      ex.getTargetException().printStackTrace();
    }
    catch (Exception ex) {
      System.out.println(
          "junit-run.jar and junit.jar \n must be in same directory or on classpath.");
      ex.printStackTrace();
    }
  }
  abstract public void runTest() throws Throwable;
  public void setFramework(DOMTestFramework framework) {
    this.framework = framework;
  }
  public void wait(int millisecond) {
    framework.wait(millisecond);
  }
  public void fail(String assertID) {
    framework.fail(this, assertID);
  }
  public void assertTrue(String assertID, boolean actual) {
    framework.assertTrue(this, assertID, actual);
  }
  public void assertTrue(String assertID, Object actual) {
    framework.assertTrue(this, assertID, ( (Boolean) actual).booleanValue());
  }
  public void assertFalse(String assertID, boolean actual) {
    framework.assertFalse(this, assertID, actual);
  }
  public void assertFalse(String assertID, Object actual) {
    framework.assertFalse(
        this,
        assertID,
        ( (Boolean) actual).booleanValue());
  }
  public void assertNull(String assertID, Object actual) {
    framework.assertNull(this, assertID, actual);
  }
  public void assertNotNull(String assertID, Object actual) {
    framework.assertNotNull(this, assertID, actual);
  }
  public void assertSame(String assertID, Object expected, Object actual) {
    framework.assertSame(this, assertID, expected, actual);
  }
  public void assertInstanceOf(String assertID, Class cls, Object obj) {
    framework.assertInstanceOf(this, assertID, obj, cls);
  }
  public void assertSize(
      String assertID,
      int expectedSize,
      NodeList collection) {
    framework.assertSize(this, assertID, expectedSize, collection);
  }
  public void assertSize(
      String assertID,
      int expectedSize,
      NamedNodeMap collection) {
    framework.assertSize(this, assertID, expectedSize, collection);
  }
  public void assertSize(
      String assertID,
      int expectedSize,
      Collection collection) {
    framework.assertSize(this, assertID, expectedSize, collection);
  }
  public void assertEqualsIgnoreCase(
      String assertID,
      String expected,
      String actual) {
    framework.assertEqualsIgnoreCase(this, assertID, expected, actual);
  }
  public void assertEqualsIgnoreCase(
      String assertID,
      Collection expected,
      Collection actual) {
    framework.assertEqualsIgnoreCase(this, assertID, expected, actual);
  }
  public void assertEqualsIgnoreCase(
      String assertID,
      List expected,
      List actual) {
    framework.assertEqualsIgnoreCase(this, assertID, expected, actual);
  }
  public void assertEqualsAutoCase(
      String context,
      String assertID,
      String expected,
      String actual) {
    String contentType = getContentType();
    if ("text/html".equals(contentType)) {
      if ("attribute".equals(context)) {
        framework.assertEqualsIgnoreCase(this, assertID, expected, actual);
      }
      else {
        framework.assertEquals(this, assertID, expected.toUpperCase(), actual);
      }
    }
    else {
      framework.assertEquals(this, assertID, expected, actual);
    }
  }
  private List toUpperCase(Collection expected) {
    List upperd = new ArrayList(expected.size());
    Iterator iter = expected.iterator();
    while (iter.hasNext()) {
      upperd.add(iter.next().toString().toUpperCase());
    }
    return upperd;
  }
  public void assertEqualAutoCase(
      String context,
      String assertID,
      Collection expected,
      Collection actual) {
    String contentType = getContentType();
    if ("text/html".equals(contentType)) {
      if ("attribute".equals(context)) {
        assertEqualsIgnoreCase(assertID, expected, actual);
      }
      else {
        framework.assertEquals(this, assertID, toUpperCase(expected), actual);
      }
    }
    else {
      framework.assertEquals(this, assertID, expected, actual);
    }
  }
  public void assertEqualsAutoCase(
      String context,
      String assertID,
      List expected,
      List actual) {
    String contentType = getContentType();
    if ("text/html".equals(contentType)) {
      if ("attribute".equals(context)) {
        assertEqualsIgnoreCase(assertID, expected, actual);
      }
      else {
        framework.assertEquals(this, assertID, toUpperCase(expected), actual);
      }
    }
    else {
      framework.assertEquals(this, assertID, expected, actual);
    }
  }
  public void assertEquals(String assertID, String expected, String actual) {
    framework.assertEquals(this, assertID, expected, actual);
  }
  public void assertEquals(String assertID, int expected, int actual) {
    framework.assertEquals(this, assertID, expected, actual);
  }
  public void assertEquals(String assertID, double expected, double actual) {
    framework.assertEquals(this, assertID, expected, actual);
  }
  public void assertEquals(
      String assertID,
      boolean expected,
      boolean actual) {
    framework.assertEquals(this, assertID, expected, actual);
  }
  public void assertEquals(
      String assertID,
      Collection expected,
      NodeList actual) {
    Collection actualList = new ArrayList();
    int actualLen = actual.getLength();
    for (int i = 0; i < actualLen; i++) {
      actualList.add(actual.item(i));
    }
    framework.assertEquals(this, assertID, expected, actualList);
  }
  public void assertEquals(
      String assertID,
      Collection expected,
      Collection actual) {
    framework.assertEquals(this, assertID, expected, actual);
  }
  public void assertNotEqualsIgnoreCase(
      String assertID,
      String expected,
      String actual) {
    framework.assertNotEqualsIgnoreCase(this, assertID, expected, actual);
  }
  public void assertNotEqualsAutoCase(
      String context,
      String assertID,
      String expected,
      String actual) {
    String contentType = getContentType();
    if ("text/html".equals(contentType)) {
      if ("attribute".equals(context)) {
        framework.assertNotEqualsIgnoreCase(this, assertID, expected, actual);
      }
      else {
        framework.assertNotEquals(this, assertID, expected.toUpperCase(),
                                  actual);
      }
    }
    framework.assertNotEquals(this, assertID, expected, actual);
  }
  public void assertNotEquals(
      String assertID,
      String expected,
      String actual) {
    framework.assertNotEquals(this, assertID, expected, actual);
  }
  public void assertNotEquals(String assertID, int expected, int actual) {
    framework.assertNotEquals(this, assertID, expected, actual);
  }
  public void assertNotEquals(
      String assertID,
      double expected,
      double actual) {
    framework.assertNotEquals(this, assertID, expected, actual);
  }
  public void assertURIEquals(
      String assertID,
      String scheme,
      String path,
      String host,
      String file,
      String name,
      String query,
      String fragment,
      Boolean isAbsolute,
      String actual) {
    assertNotNull(assertID, actual);
    String uri = actual;
    int lastPound = actual.lastIndexOf("#");
    String actualFragment = "";
    if (lastPound != -1) {
      uri = actual.substring(0, lastPound);
      actualFragment = actual.substring(lastPound + 1);
    }
    if (fragment != null) {
      assertEquals(assertID, fragment, actualFragment);
    }
    int lastQuestion = uri.lastIndexOf("?");
    String actualQuery = "";
    if (lastQuestion != -1) {
      uri = actual.substring(0, lastQuestion);
      actualQuery = actual.substring(lastQuestion + 1);
    }
    if (query != null) {
      assertEquals(assertID, query, actualQuery);
    }
    int firstColon = uri.indexOf(":");
    int firstSlash = uri.indexOf("/");
    String actualPath = uri;
    String actualScheme = "";
    if (firstColon != -1 && firstColon < firstSlash) {
      actualScheme = uri.substring(0, firstColon);
      actualPath = uri.substring(firstColon + 1);
    }
    if (scheme != null) {
      assertEquals(assertID, scheme, actualScheme);
    }
    if (path != null) {
      assertEquals(assertID, path, actualPath);
    }
    if (host != null) {
      String actualHost = "";
      if (actualPath.startsWith("
        int termSlash = actualPath.indexOf("/", 2);
        actualHost = actualPath.substring(0, termSlash);
      }
      assertEquals(assertID, host, actualHost);
    }
    String actualFile = actualPath;
    if (file != null || name != null) {
      int finalSlash = actualPath.lastIndexOf("/");
      if (finalSlash != -1) {
        actualFile = actualPath.substring(finalSlash + 1);
      }
      if (file != null) {
        assertEquals(assertID, file, actualFile);
      }
    }
    if (name != null) {
      String actualName = actualFile;
      int finalPeriod = actualFile.lastIndexOf(".");
      if (finalPeriod != -1) {
        actualName = actualFile.substring(0, finalPeriod);
      }
      assertEquals(assertID, name, actualName);
    }
    if (isAbsolute != null) {
      assertEquals(
          assertID,
          isAbsolute.booleanValue(),
          actualPath.startsWith("/") || actualPath.startsWith("file:/"));
    }
  }
  public boolean same(Object expected, Object actual) {
    return framework.same(expected, actual);
  }
  public boolean equalsIgnoreCase(String expected, String actual) {
    return framework.equalsIgnoreCase(expected, actual);
  }
  public boolean equalsIgnoreCase(Collection expected, Collection actual) {
    return framework.equalsIgnoreCase(expected, actual);
  }
  public boolean equalsIgnoreCase(List expected, List actual) {
    return framework.equalsIgnoreCase(expected, actual);
  }
  public boolean equalsAutoCase(String context, String expected, String actual) {
    if ("text/html".equals(getContentType())) {
      if ("attribute".equals(context)) {
        return framework.equalsIgnoreCase(expected, actual);
      }
      else {
        return framework.equals(expected.toUpperCase(), actual);
      }
    }
    return framework.equals(expected, actual);
  }
  public boolean equalsAutoCase(String context, Collection expected,
                                Collection actual) {
    if ("text/html".equals(getContentType())) {
      if ("attribute".equals(context)) {
        return framework.equalsIgnoreCase(expected, actual);
      }
      else {
        return framework.equals(toUpperCase(expected), actual);
      }
    }
    return framework.equals(expected, actual);
  }
  public boolean equalsAutoCase(String context, List expected, List actual) {
    if ("text/html".equals(getContentType())) {
      if ("attribute".equals(context)) {
        return framework.equalsIgnoreCase(expected, actual);
      }
      else {
        return framework.equals(toUpperCase(expected), actual);
      }
    }
    return framework.equals(expected, actual);
  }
  public boolean equals(String expected, String actual) {
    return framework.equals(expected, actual);
  }
  public boolean equals(int expected, int actual) {
    return framework.equals(expected, actual);
  }
  public boolean equals(double expected, double actual) {
    return framework.equals(expected, actual);
  }
  public boolean equals(Collection expected, Collection actual) {
    return framework.equals(expected, actual);
  }
  public boolean equals(List expected, List actual) {
    return framework.equals(expected, actual);
  }
  public int size(Collection collection) {
    return framework.size(collection);
  }
  public int size(NamedNodeMap collection) {
    return framework.size(collection);
  }
  public int size(NodeList collection) {
    return framework.size(collection);
  }
}

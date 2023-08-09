public class MediaFrameworkPerfTestRunner extends InstrumentationTestRunner {
  @Override
  public TestSuite getAllTests() {
      TestSuite suite = new InstrumentationTestSuite(this);
      suite.addTestSuite(MediaPlayerPerformance.class);
      return suite;
  }
  @Override
  public ClassLoader getLoader() {
      return MediaFrameworkTestRunner.class.getClassLoader();
  }
}

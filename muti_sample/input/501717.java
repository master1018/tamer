public class MusicPlayerStressTestRunner extends InstrumentationTestRunner {
  @Override
    public TestSuite getAllTests() {
      TestSuite suite = new InstrumentationTestSuite(this);  
      suite.addTestSuite(AlbumsPlaybackStress.class);
      return suite;
    }
    @Override
    public ClassLoader getLoader() {
      return MusicPlayerStressTestRunner.class.getClassLoader();
    }
}

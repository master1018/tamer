public class MusicPlayerFunctionalTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);  
        suite.addTestSuite(TestSongs.class);
        suite.addTestSuite(TestPlaylist.class);
        suite.addTestSuite(MusicPlayerStability.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return MusicPlayerFunctionalTestRunner.class.getClassLoader();
    }
}

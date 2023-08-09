public class XmlConfigUtilsTest extends TestCase {
    public XmlConfigUtilsTest(String testName) {
        super(testName);
    }
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlConfigUtilsTest.class);
        return suite;
    }
    public void testWriteToFile() throws Exception {
        System.out.println("writeToFile");
        final File file = File.createTempFile("test",".xml");
        file.deleteOnExit();
        final String tmp = System.getProperty("java.io.tmpdir");
        DirectoryScannerConfig dir1 =
                new DirectoryScannerConfig("scan2");
        dir1.setRootDirectory(tmp);
        ScanManagerConfig bean = new ScanManagerConfig("session2");
        bean.putScan(dir1);
        XmlConfigUtils instance = new XmlConfigUtils(file.getPath());
        instance.writeToFile(bean);
    }
    public void testReadFromFile() throws Exception {
        System.out.println("readFromFile");
        final String tmp = System.getProperty("java.io.tmpdir");
        final File file = File.createTempFile("test",".xml");
        file.deleteOnExit();
        DirectoryScannerConfig dir1 =
                new DirectoryScannerConfig("scan1");
        dir1.setRootDirectory(tmp);
        ScanManagerConfig bean = new ScanManagerConfig("session1");
        bean.putScan(dir1);
        XmlConfigUtils instance = new XmlConfigUtils(file.getPath());
        instance.writeToFile(bean);
        ScanManagerConfig expResult = bean;
        ScanManagerConfig result = instance.readFromFile();
        System.out.println(result);
        assertEquals(expResult, result);
    }
}

public class ScanDirConfigTest extends TestCase {
    public ScanDirConfigTest(String testName) {
        super(testName);
    }
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(ScanDirConfigTest.class);
        return suite;
    }
    public void testLoad() throws Exception {
        System.out.println("load");
        final File file = File.createTempFile("testconf",".xml");
        final ScanDirConfig instance = new ScanDirConfig(file.getAbsolutePath());
        final ScanManagerConfig bean =
                new  ScanManagerConfig("testLoad");
        final DirectoryScannerConfig dir =
                new DirectoryScannerConfig("tmp");
        dir.setRootDirectory(file.getParent());
        bean.putScan(dir);
        XmlConfigUtils.write(bean,new FileOutputStream(file),false);
        instance.load();
        assertEquals(bean,instance.getConfiguration());
        bean.removeScan(dir.getName());
        XmlConfigUtils.write(bean,new FileOutputStream(file),false);
        assertNotSame(bean,instance.getConfiguration());
        instance.load();
        assertEquals(bean,instance.getConfiguration());
    }
    public void testSave() throws Exception {
        System.out.println("save");
        final File file = File.createTempFile("testconf",".xml");
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ScanManagerMXBean manager = ScanManager.register(mbs);
        try {
            final ScanDirConfigMXBean instance =
                    manager.createOtherConfigurationMBean("testSave",file.getAbsolutePath());
            assertTrue(mbs.isRegistered(
                    ScanManager.makeScanDirConfigName("testSave")));
            final ScanManagerConfig bean =
                new  ScanManagerConfig("testSave");
            final DirectoryScannerConfig dir =
                new DirectoryScannerConfig("tmp");
            dir.setRootDirectory(file.getParent());
            bean.putScan(dir);
            instance.setConfiguration(bean);
            instance.save();
            final ScanManagerConfig loaded =
                new XmlConfigUtils(file.getAbsolutePath()).readFromFile();
            assertEquals(instance.getConfiguration(),loaded);
            assertEquals(bean,loaded);
            instance.getConfiguration().removeScan("tmp");
            instance.save();
            assertNotSame(loaded,instance.getConfiguration());
            final ScanManagerConfig loaded2 =
                new XmlConfigUtils(file.getAbsolutePath()).readFromFile();
            assertEquals(instance.getConfiguration(),loaded2);
        } finally {
            manager.close();
            mbs.unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
        }
        final ObjectName all =
                new ObjectName(ScanManager.SCAN_MANAGER_NAME.getDomain()+":*");
        assertEquals(0,mbs.queryNames(all,null).size());
    }
    public void testGetXmlConfigString() throws Exception {
        System.out.println("getXmlConfigString");
        try {
            final File file = File.createTempFile("testconf",".xml");
            final ScanDirConfig instance = new ScanDirConfig(file.getAbsolutePath());
            final ScanManagerConfig bean =
                new  ScanManagerConfig("testGetXmlConfigString");
            final DirectoryScannerConfig dir =
                new DirectoryScannerConfig("tmp");
            dir.setRootDirectory(file.getParent());
            bean.putScan(dir);
            instance.setConfiguration(bean);
            System.out.println("Expected: " + XmlConfigUtils.toString(bean));
            System.out.println("Received: " +
                    instance.getConfiguration().toString());
            assertEquals(XmlConfigUtils.toString(bean),
                instance.getConfiguration().toString());
        } catch (Exception x) {
            x.printStackTrace();
            throw x;
        }
    }
    public void testAddNotificationListener() throws Exception {
        System.out.println("addNotificationListener");
        final File file = File.createTempFile("testconf",".xml");
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ScanManagerMXBean manager = ScanManager.register(mbs);
        try {
            final ScanDirConfigMXBean instance =
                TestUtils.makeNotificationEmitter(
                    manager.createOtherConfigurationMBean("testSave",
                        file.getAbsolutePath()),
                    ScanDirConfigMXBean.class);
            assertTrue(mbs.isRegistered(
                    ScanManager.makeScanDirConfigName("testSave")));
            DirectoryScannerConfig dir =
                    instance.addDirectoryScanner("tmp",file.getParent(),".*",0,0);
            final BlockingQueue<Notification> queue =
                    new LinkedBlockingQueue<Notification>();
            final NotificationListener listener = new NotificationListener() {
                public void handleNotification(Notification notification,
                            Object handback) {
                    queue.add(notification);
                }
            };
            NotificationFilter filter = null;
            Object handback = null;
            ((NotificationEmitter)instance).addNotificationListener(listener,
                    filter, handback);
            instance.save();
            final ScanManagerConfig loaded =
                new XmlConfigUtils(file.getAbsolutePath()).readFromFile();
            assertEquals(instance.getConfiguration(),loaded);
            final ScanManagerConfig newConfig =
                    instance.getConfiguration();
            newConfig.removeScan("tmp");
            instance.setConfiguration(newConfig);
            instance.save();
            assertNotSame(loaded,instance.getConfiguration());
            final ScanManagerConfig loaded2 =
                new XmlConfigUtils(file.getAbsolutePath()).readFromFile();
            assertEquals(instance.getConfiguration(),loaded2);
            instance.load();
            for (int i=0;i<4;i++) {
                final Notification n = queue.poll(3,TimeUnit.SECONDS);
                assertNotNull(n);
                assertEquals(TestUtils.getObjectName(instance),n.getSource());
                switch(i) {
                    case 0: case 2:
                        assertEquals(ScanDirConfig.NOTIFICATION_SAVED,n.getType());
                        break;
                    case 1:
                        assertEquals(ScanDirConfig.NOTIFICATION_MODIFIED,n.getType());
                        break;
                    case 3:
                        assertEquals(ScanDirConfig.NOTIFICATION_LOADED,n.getType());
                        break;
                    default: break;
                }
            }
        } finally {
            manager.close();
            mbs.unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
        }
        final ObjectName all =
                new ObjectName(ScanManager.SCAN_MANAGER_NAME.getDomain()+":*");
        assertEquals(0,mbs.queryNames(all,null).size());
    }
    public void testGetConfigFilename() throws Exception {
        System.out.println("getConfigFilename");
        final File file = File.createTempFile("testconf",".xml");
        final ScanDirConfig instance = new ScanDirConfig(file.getAbsolutePath());
        String result = instance.getConfigFilename();
        assertEquals(file.getAbsolutePath(), new File(result).getAbsolutePath());
    }
    public void testAddDirectoryScanner() throws IOException {
        System.out.println("addDirectoryScanner");
        System.out.println("save");
        final File file = File.createTempFile("testconf",".xml");
        final ScanDirConfig instance = new ScanDirConfig(file.getAbsolutePath());
        final ScanManagerConfig bean =
                new  ScanManagerConfig("testSave");
        final DirectoryScannerConfig dir =
                new DirectoryScannerConfig("tmp");
        dir.setRootDirectory(file.getParent());
        FileMatch filter = new FileMatch();
        filter.setFilePattern(".*");
        dir.setIncludeFiles(new FileMatch[] {
            filter
        });
        instance.setConfiguration(bean);
        instance.addDirectoryScanner(dir.getName(),
                                     dir.getRootDirectory(),
                                     filter.getFilePattern(),
                                     filter.getSizeExceedsMaxBytes(),
                                     0);
        instance.save();
        final ScanManagerConfig loaded =
                new XmlConfigUtils(file.getAbsolutePath()).readFromFile();
        assertNotNull(loaded.getScan(dir.getName()));
        assertEquals(dir,loaded.getScan(dir.getName()));
        assertEquals(instance.getConfiguration(),loaded);
        assertEquals(instance.getConfiguration().getScan(dir.getName()),dir);
    }
}

public class ConfigMatchTest extends TestCase {
    private static final String SEARCHED_FILENAME = "main.xml"; 
    private static final String MISC1_FILENAME = "foo.xml"; 
    private static final String MISC2_FILENAME = "bar.xml"; 
    private ProjectResources mResources;
    private ResourceQualifier[] mQualifierList;
    private FolderConfiguration config4;
    private FolderConfiguration config3;
    private FolderConfiguration config2;
    private FolderConfiguration config1;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ResourceManager manager = ResourceManager.getInstance();
        Field qualifierListField = ResourceManager.class.getDeclaredField("mQualifiers");
        assertNotNull(qualifierListField);
        qualifierListField.setAccessible(true);
        mQualifierList = (ResourceQualifier[])qualifierListField.get(manager);
        mResources = new ProjectResources(null );
        FileMock[] validMemberList = new FileMock[] {
                new FileMock(MISC1_FILENAME),
                new FileMock(SEARCHED_FILENAME),
                new FileMock(MISC2_FILENAME),
        };
        FileMock[] invalidMemberList = new FileMock[] {
                new FileMock(MISC1_FILENAME),
                new FileMock(MISC2_FILENAME),
        };
        FolderConfiguration defaultConfig = getConfiguration(
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null);
        addFolder(mResources, defaultConfig, validMemberList);
        config1 = getConfiguration(
                null, 
                null, 
                "en", 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                KeyboardState.EXPOSED.getValue(), 
                null, 
                null, 
                null, 
                null);
        addFolder(mResources, config1, validMemberList);
        config2 = getConfiguration(
                null, 
                null, 
                "en", 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                KeyboardState.HIDDEN.getValue(), 
                null, 
                null, 
                null, 
                null);
        addFolder(mResources, config2, validMemberList);
        config3 = getConfiguration(
                null, 
                null, 
                "en", 
                null, 
                null, 
                null, 
                ScreenOrientation.LANDSCAPE.getValue(), 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null);
        addFolder(mResources, config3, validMemberList);
        config4 = getConfiguration(
                "mcc310", 
                "mnc435", 
                "en", 
                "rUS", 
                "normal", 
                "notlong", 
                ScreenOrientation.LANDSCAPE.getValue(), 
                "mdpi", 
                TouchScreenType.FINGER.getValue(), 
                KeyboardState.EXPOSED.getValue(), 
                TextInputMethod.QWERTY.getValue(), 
                NavigationMethod.DPAD.getValue(), 
                "480x320", 
                "v3"); 
        addFolder(mResources, config4, invalidMemberList);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mResources = null;
    }
    public void test1() {
        FolderConfiguration testConfig = getConfiguration(
                "mcc310", 
                "mnc435", 
                "en", 
                "rUS", 
                "normal", 
                "notlong", 
                ScreenOrientation.LANDSCAPE.getValue(), 
                "mdpi", 
                TouchScreenType.FINGER.getValue(), 
                KeyboardState.EXPOSED.getValue(), 
                TextInputMethod.QWERTY.getValue(), 
                NavigationMethod.DPAD.getValue(), 
                "480x320", 
                "v3"); 
        ResourceFile result = mResources.getMatchingFile(SEARCHED_FILENAME,
                ResourceFolderType.LAYOUT, testConfig);
        boolean bresult = result.getFolder().getConfiguration().equals(config3);
        assertEquals(bresult, true);
    }
    private FolderConfiguration getConfiguration(String... qualifierValues) {
        FolderConfiguration config = new FolderConfiguration();
        assertEquals(qualifierValues.length, mQualifierList.length);
        int index = 0;
        for (ResourceQualifier qualifier : mQualifierList) {
            String value = qualifierValues[index++];
            if (value != null) {
                assertTrue(qualifier.checkAndSet(value, config));
            }
        }
        return config;
    }
    private void addFolder(ProjectResources resources, FolderConfiguration config,
            FileMock[] memberList) throws Exception {
        String folderName = config.getFolderName(ResourceFolderType.LAYOUT, (IAndroidTarget)null);
        FolderMock folder = new FolderMock(folderName, memberList);
        ResourceFolder resFolder = _addProjectResourceFolder(resources, config, folder);
        for (FileMock file : memberList) {
            resFolder.addFile(new SingleResourceFile(new IFileWrapper(file), resFolder));
        }
    }
    private ResourceFolder _addProjectResourceFolder(ProjectResources resources,
            FolderConfiguration config, FolderMock folder) throws Exception {
        Method addMethod = ProjectResources.class.getDeclaredMethod("add",
                ResourceFolderType.class, FolderConfiguration.class,
                IAbstractFolder.class);
        addMethod.setAccessible(true);
        ResourceFolder resFolder = (ResourceFolder)addMethod.invoke(resources,
                ResourceFolderType.LAYOUT, config, new IFolderWrapper(folder));
        return resFolder;
    }
}

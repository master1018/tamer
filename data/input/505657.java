public class QualifierListTest extends TestCase {
    private ResourceManager mManager;
    @Override
    public void setUp()  throws Exception {
        super.setUp();
        mManager = ResourceManager.getInstance();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mManager = null;
    }
    public void testQualifierList() {
        try {
            Field qualifierListField = ResourceManager.class.getDeclaredField("mQualifiers");
            assertNotNull(qualifierListField);
            qualifierListField.setAccessible(true);
            ResourceQualifier[] qualifierList =
                (ResourceQualifier[])qualifierListField.get(mManager);
            Field qualCountField = FolderConfiguration.class.getDeclaredField("INDEX_COUNT");
            assertNotNull(qualCountField);
            qualCountField.setAccessible(true);
            Integer count = (Integer)qualCountField.get(null);
            assertEquals(count.intValue(), qualifierList.length);
        } catch (SecurityException e) {
            assertTrue(false);
        } catch (NoSuchFieldException e) {
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        } catch (IllegalAccessException e) {
            assertTrue(false);
        }
    }
}

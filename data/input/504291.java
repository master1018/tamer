public class PhotoPriorityResolverTest extends AndroidTestCase {
    public void testLoadPicturePriorityFromXml() {
        PhotoPriorityResolver resolver = new PhotoPriorityResolver(getContext());
        assertEquals(42,
                resolver.resolvePhotoPriorityFromMetaData("com.android.providers.contacts.tests"));
        assertEquals(PhotoPriorityResolver.DEFAULT_PRIORITY,
                resolver.resolvePhotoPriorityFromMetaData("no.such.package"));
    }
}

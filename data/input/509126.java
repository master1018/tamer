@TestTargetClass(MalformedMimeTypeException.class)
public class IntentFilter_MalformedMimeTypeExceptionTest extends
        AndroidTestCase {
    private MalformedMimeTypeException mMalformedMimeTypeException;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMalformedMimeTypeException = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter.MalformedMimeTypeException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentFilter.MalformedMimeTypeException",
            args = {java.lang.String.class}
        )
    })
    public void testMalformedMimeTypeException() {
        mMalformedMimeTypeException = new IntentFilter.MalformedMimeTypeException();
        assertNotNull(mMalformedMimeTypeException);
        try {
            throw mMalformedMimeTypeException;
        } catch (MalformedMimeTypeException e) {
        }
        final String message = "testException";
        mMalformedMimeTypeException = new IntentFilter.MalformedMimeTypeException(
                message);
        assertNotNull(mMalformedMimeTypeException);
        assertEquals(message, mMalformedMimeTypeException.getMessage());
        try {
            throw mMalformedMimeTypeException;
        } catch (MalformedMimeTypeException e) {
            assertEquals(message, e.getMessage());
        }
    }
}

@TestTargetClass(ParcelFileDescriptor.AutoCloseInputStream.class)
public class ParcelFileDescriptor_AutoCloseInputStreamTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AutoCloseInputStream",
            args = {android.os.ParcelFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        )
    })
    public void testAutoCloseInputStream() throws Exception {
        ParcelFileDescriptor pf = ParcelFileDescriptorTest.makeParcelFileDescriptor(getContext());
        AutoCloseInputStream in = new AutoCloseInputStream(pf);
        assertEquals(0, in.read());
        in.close();
        try {
            in.read();
            fail("Failed to throw exception.");
        } catch (IOException e) {
        }
    }
}

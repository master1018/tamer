@TestTargetClass(ParcelFileDescriptor.AutoCloseOutputStream.class)
public class ParcelFileDescriptor_AutoCloseOutputStreamTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link AutoCloseOutputStream}",
            method = "AutoCloseOutputStream",
            args = {android.os.ParcelFileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: close",
            method = "close",
            args = {}
        )
    })
    public void testAutoCloseOutputStream() throws Exception {
        ParcelFileDescriptor pf = ParcelFileDescriptorTest.makeParcelFileDescriptor(getContext());
        AutoCloseOutputStream out = new AutoCloseOutputStream(pf);
        out.write(2);
        out.close();
        try {
            out.write(2);
            fail("Failed to throw exception.");
        } catch (IOException e) {
        }
    }
}

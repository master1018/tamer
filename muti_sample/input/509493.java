@TestTargetClass(BitmapFactory.Options.class)
public class BitmapFactory_OptionsTest extends AndroidTestCase{
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "BitmapFactory.Options",
        args = {}
    )
    public void testOptions(){
        new BitmapFactory.Options();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestCancelDecode",
        args = {}
    )
    public void testRequestCancelDecode(){
        BitmapFactory.Options option = new BitmapFactory.Options();
        assertFalse(option.mCancel);
        option.requestCancelDecode();
        assertTrue(option.mCancel);
    }
}

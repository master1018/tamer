public class BrickDeniedTest extends AndroidTestCase {
    @MediumTest
    public void testBrick() {
        getContext().sendBroadcast(new Intent("SHES_A_BRICK_HOUSE"));
        getContext().sendBroadcast(new Intent("android.intent.action.BRICK"));
    }
}

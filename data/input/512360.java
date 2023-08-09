@TestTargetClass(AdapterContextMenuInfo.class)
public class AdapterView_AdapterContextMenuInfoTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AdapterView.AdapterContextMenuInfo",
        args = {android.view.View.class, int.class, long.class}
    )
    public void testConstructor() {
        AdapterView.AdapterContextMenuInfo menuInfo;
        View testView = new View(getContext());
        int position = 1;
        long id = 0xffL;
        menuInfo = new AdapterView.AdapterContextMenuInfo(testView, position, id);
        assertEquals(position, menuInfo.position);
        assertEquals(id, menuInfo.id);
        assertEquals(testView, menuInfo.targetView);
    }
}

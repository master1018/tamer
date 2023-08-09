public class WeightTest extends ActivityInstrumentationTestCase<Weight> {
    private View mCell1;
    private View mCell2;
    private View mCell3;
    private View mRow;
    public WeightTest() {
        super("com.android.frameworks.coretests", Weight.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Weight activity = getActivity();
        mCell1 = activity.findViewById(R.id.cell1);
        mCell3 = activity.findViewById(R.id.cell2);
        mCell2 = activity.findViewById(R.id.cell3);
        mRow = activity.findViewById(R.id.row);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mCell1);
        assertNotNull(mCell2);
        assertNotNull(mCell3);
        assertNotNull(mRow);
    }
    @MediumTest
    public void testAllCellsFillParent() throws Exception {
        assertEquals(mCell1.getWidth() + mCell2.getWidth() + mCell3.getWidth(), mRow.getWidth());
    }
}

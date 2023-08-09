public class SnakeTest extends ActivityInstrumentationTestCase2<Snake> {
    public SnakeTest() {
        super(Snake.class);
    }
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}

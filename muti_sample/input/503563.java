public class LunarLanderTest extends ActivityInstrumentationTestCase2<LunarLander> {
    public LunarLanderTest() {
        super(LunarLander.class);
    }
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}

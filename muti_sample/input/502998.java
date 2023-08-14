public class NotePadTest extends ActivityInstrumentationTestCase2<NotesList> {
    public NotePadTest() {
        super(NotesList.class);
    }
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}

@TestTargetClass(android.provider.Contacts.Organizations.class)
public class Contacts_OrganizationsTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDisplayLabel(Context context, int type, CharSequence label",
        method = "getDisplayLabel",
        args = {android.content.Context.class, int.class, java.lang.CharSequence.class}
    )
    public void testGetDisplayLabel() {
        String label = "label";
        String display = Organizations.getDisplayLabel(getContext(),
                Organizations.TYPE_CUSTOM, label).toString();
        assertEquals(label, display);
        CharSequence[] labels = getContext().getResources().getTextArray(
                com.android.internal.R.array.organizationTypes);
        display = Organizations.getDisplayLabel(getContext(),
                Organizations.TYPE_OTHER, label).toString();
        assertEquals(labels[Organizations.TYPE_OTHER - 1], display);
        display = Organizations.getDisplayLabel(getContext(),
                Organizations.TYPE_WORK, label).toString();
        assertEquals(labels[Organizations.TYPE_WORK - 1], display);
    }
}

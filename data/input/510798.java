@TestTargetClass(android.provider.Contacts.Phones.class)
public class Contacts_PhonesTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDisplayLabel(Context context, int type,  CharSequence label)",
        method = "getDisplayLabel",
        args = {android.content.Context.class, int.class, java.lang.CharSequence.class}
    )
    public void testGetDisplayLabel() {
        CharSequence label = "label";
        String display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_CUSTOM, label).toString();
        assertEquals(label, display);
        CharSequence[] labels = getContext().getResources().getTextArray(
                com.android.internal.R.array.phoneTypes);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_HOME, label).toString();
        assertEquals(labels[Phones.TYPE_HOME - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_MOBILE, label).toString();
        assertEquals(labels[Phones.TYPE_MOBILE - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_WORK, label).toString();
        assertEquals(labels[Phones.TYPE_WORK - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_FAX_WORK, label).toString();
        assertEquals(labels[Phones.TYPE_FAX_WORK - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_FAX_HOME, label).toString();
        assertEquals(labels[Phones.TYPE_FAX_HOME - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_PAGER, label).toString();
        assertEquals(labels[Phones.TYPE_PAGER - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_OTHER, label).toString();
        assertEquals(labels[Phones.TYPE_OTHER - 1], display);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDisplayLabel which need specify a labelArray",
        method = "getDisplayLabel",
        args = {android.content.Context.class, int.class, java.lang.CharSequence.class, 
                java.lang.CharSequence[].class}
    )
    public void testGetDisplayLabelCharSequenceArray() {
        CharSequence label = "label";
        CharSequence[] labelArray = new CharSequence[] {
                "1 home",
                "2 mobile",
                "3 work",
                "4 fax work",
                "5 fax home",
                "6 pager",
                "7 other"};
        String display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_CUSTOM, label, labelArray).toString();
        assertEquals(label, display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_HOME, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_HOME - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_MOBILE, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_MOBILE - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_WORK, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_WORK - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_FAX_WORK, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_FAX_WORK - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_FAX_HOME, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_FAX_HOME - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_PAGER, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_PAGER - 1], display);
        display = Phones.getDisplayLabel(getContext(),
                Phones.TYPE_OTHER, label, labelArray).toString();
        assertEquals(labelArray[Phones.TYPE_OTHER - 1], display);
    }
}

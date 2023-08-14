@TestTargetClass(android.provider.Contacts.ContactMethods.class)
public class Contacts_ContactMethodsTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test addPostalLocation function",
        method = "addPostalLocation",
        args = {android.content.Context.class, long.class, double.class, double.class}
    )
    @ToBeFixed(explanation = "This function is not static, but we can't new a" +
            " instance to call it because the constructor is private.")
    public void testAddPostalLocation() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which encode or decode protocol",
            method = "encodePredefinedImProtocol",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which encode or decode protocol",
            method = "encodeCustomImProtocol",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which encode or decode protocol",
            method = "decodeImProtocol",
            args = {java.lang.String.class}
        )
    })
    public void testEncodeAndDecodeProtocol() {
        int protocol = ContactMethods.PROTOCOL_AIM;
        String encodedString = ContactMethods.encodePredefinedImProtocol(protocol);
        assertTrue(encodedString.startsWith("pre:"));
        assertEquals(protocol, ContactMethods.decodeImProtocol(encodedString));
        protocol = ContactMethods.PROTOCOL_QQ;
        encodedString = ContactMethods.encodePredefinedImProtocol(protocol);
        assertTrue(encodedString.startsWith("pre:"));
        assertEquals(protocol, ContactMethods.decodeImProtocol(encodedString));
        String protocolString = "custom protocol";
        encodedString = ContactMethods.encodeCustomImProtocol(protocolString);
        assertTrue(encodedString.startsWith("custom:"));
        assertEquals(protocolString, ContactMethods.decodeImProtocol(encodedString));
        try {
            ContactMethods.decodeImProtocol("wrong format");
            fail("Should throw IllegalArgumentException when the format is wrong.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDisplayLabel(Context context, int kind, int type, CharSequence label)",
        method = "getDisplayLabel",
        args = {android.content.Context.class, int.class, int.class, java.lang.CharSequence.class}
    )
    public void test() {
        String label = "label";
        String display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_EMAIL,
                ContactMethods.TYPE_CUSTOM, label).toString();
        assertEquals(label, display);
        CharSequence[] labels = getContext().getResources().getTextArray(
                com.android.internal.R.array.emailAddressTypes);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_EMAIL,
                ContactMethods.TYPE_HOME, label).toString();
        assertEquals(labels[ContactMethods.TYPE_HOME - 1], display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_EMAIL,
                ContactMethods.TYPE_OTHER, label).toString();
        assertEquals(labels[ContactMethods.TYPE_OTHER - 1], display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_EMAIL,
                ContactMethods.TYPE_WORK, label).toString();
        assertEquals(labels[ContactMethods.TYPE_WORK - 1], display);
        String untitled = getContext().getString(R.string.untitled);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_IM,
                ContactMethods.TYPE_CUSTOM, label).toString();
        assertEquals(untitled, display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_ORGANIZATION,
                ContactMethods.TYPE_CUSTOM, label).toString();
        assertEquals(untitled, display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_PHONE,
                ContactMethods.TYPE_CUSTOM, label).toString();
        assertEquals(untitled, display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_POSTAL,
                ContactMethods.TYPE_CUSTOM, label).toString();
        assertEquals(label, display);
        labels = getContext().getResources().getTextArray(
                com.android.internal.R.array.postalAddressTypes);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_POSTAL,
                ContactMethods.TYPE_HOME, label).toString();
        assertEquals(labels[ContactMethods.TYPE_HOME - 1], display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_POSTAL,
                ContactMethods.TYPE_OTHER, label).toString();
        assertEquals(labels[ContactMethods.TYPE_OTHER - 1], display);
        display = ContactMethods.getDisplayLabel(getContext(), Contacts.KIND_POSTAL,
                ContactMethods.TYPE_WORK, label).toString();
        assertEquals(labels[ContactMethods.TYPE_WORK - 1], display);
    }
}

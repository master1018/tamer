@TestTargetClass(UrlQuerySanitizer.IllegalCharacterValueSanitizer.class)
public class UrlQuerySanitizer_IllegalCharacterValueSanitizerTest extends AndroidTestCase {
    static final int SPACE_OK = IllegalCharacterValueSanitizer.SPACE_OK;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link IllegalCharacterValueSanitizer}",
            method = "IllegalCharacterValueSanitizer",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: sanitize",
            method = "sanitize",
            args = {String.class}
        )
    })
    public void testSanitize() {
        IllegalCharacterValueSanitizer sanitizer =  new IllegalCharacterValueSanitizer(SPACE_OK);
        assertEquals("Joe User", sanitizer.sanitize("Joe<User"));
    }
}

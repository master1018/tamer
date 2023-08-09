@TestTargetClass(Pattern.class)
public class PatternErrorTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) and compile(String regex, int flag) method with invalid parameters. Doesn't verify IllegalArgumentException, PatternSyntaxException.",
            method = "compile",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies compile(String regex) and compile(String regex, int flag) method with invalid parameters. Doesn't verify IllegalArgumentException, PatternSyntaxException.",
            method = "compile",
            args = {java.lang.String.class, int.class}
        )
    })    
    public void testCompileErrors() throws Exception {
        try {
            Pattern.compile(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        Pattern.compile("");
        int flags = 0;
        Pattern.compile("foo", flags);
        flags |= Pattern.UNIX_LINES;
        flags |= Pattern.CASE_INSENSITIVE;
        flags |= Pattern.MULTILINE;
        flags |= Pattern.COMMENTS;
        flags |= Pattern.DOTALL;
        flags |= Pattern.UNICODE_CASE;
        Pattern.compile("foo", flags);
        flags |= ~Pattern.CANON_EQ;
        Pattern.compile("foo", flags);
    }
}

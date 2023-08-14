@TestTargetClass(Rfc822Tokenizer.class)
public class Rfc822TokenizerTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Rfc822Tokenizer",
        args = {}
    )
    public void testConstructor() {
        new Rfc822Tokenizer();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findTokenStart",
        args = {java.lang.CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for findTokenStart() is incomplete." +
            "1. not clear what is supposed to happen if text is null.")
    public void testFindTokenStart() {
        Rfc822Tokenizer rfc822Tokenizer = new Rfc822Tokenizer();
        String token1 = "John Doe <janed@example.net> (work)";
        String token2 = "Ann Lee <annlee@example.com> (secret)";
        String token3 = "Charles Hanson (work)";
        String token4 = "Lucia Smith <lsmith@example.com>";
        String text = ",   " + token1 + "\",\"" + token2 + ";" + token3 + " <;>" + token4;
        final int TOKEN_START_POS_1 = text.indexOf(token1);
        final int TOKEN_START_POS_2 = text.indexOf(token2);
        final int TOKEN_START_POS_3 = text.indexOf(token3);
        final int TOKEN_START_POS_4 = text.indexOf(token4);
        assertEquals(0, rfc822Tokenizer.findTokenStart(text, 0));
        assertEquals(0, rfc822Tokenizer.findTokenStart(text, TOKEN_START_POS_1));
        assertEquals(TOKEN_START_POS_1, rfc822Tokenizer.findTokenStart(text, TOKEN_START_POS_2));
        assertEquals(TOKEN_START_POS_1, rfc822Tokenizer.findTokenStart(text, TOKEN_START_POS_3));
        assertEquals(TOKEN_START_POS_3, rfc822Tokenizer.findTokenStart(text, TOKEN_START_POS_4));
        assertEquals(TOKEN_START_POS_3, rfc822Tokenizer.findTokenStart(text, text.length()));
        assertEquals(0, rfc822Tokenizer.findTokenStart(text, -1));
        assertEquals(TOKEN_START_POS_3, rfc822Tokenizer.findTokenStart(text, text.length() + 1));
        try {
            rfc822Tokenizer.findTokenStart(null, TOKEN_START_POS_1);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findTokenEnd",
        args = {java.lang.CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for findTokenEnd() is incomplete." +
            "1. not clear what is supposed to happen if text is null." +
            "2. not clear whether it is supposed result if cursor is exceptional value.")
    public void testFindTokenEnd() {
        Rfc822Tokenizer rfc822Tokenizer = new Rfc822Tokenizer();
        String token1 = "John Doe <janed@example.net> (work)";
        String text1 = ",   " + token1;
        assertEquals(0, rfc822Tokenizer.findTokenEnd(text1, 0));
        assertEquals(text1.length(), rfc822Tokenizer.findTokenEnd(text1, 1));
        assertEquals(Integer.MAX_VALUE, rfc822Tokenizer.findTokenEnd(text1, Integer.MAX_VALUE));
        try {
            rfc822Tokenizer.findTokenEnd(text1, -1);
            fail("Should throw IndexOutOfBoundsException!");
        } catch (IndexOutOfBoundsException e) {
        }
        String token2 = "Ann Lee <annlee@example.com> (secret)";
        String token3 = "Charles Hanson (work)";
        String token4 = "Lucia Smith <lsmith@example.com>";
        String text2 = token1 + "\",\"" + token2 + ";" + token3 + " <;>" + token4 + ",";
        final int TOKEN_END_POS_2 = text2.indexOf(token2) + token2.length();
        final int TOKEN_END_POS_4 = text2.indexOf(token4) + token4.length();
        assertEquals(TOKEN_END_POS_2, rfc822Tokenizer.findTokenEnd(text2, 0));
        assertEquals(TOKEN_END_POS_4, rfc822Tokenizer.findTokenEnd(text2, TOKEN_END_POS_2 + 1));
        try {
            rfc822Tokenizer.findTokenEnd(null, 0);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "terminateToken",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for terminateToken() is incomplete." +
            "1. not clear what is supposed result if text is null.")
    public void testTerminateToken() {
        Rfc822Tokenizer rfc822Tokenizer = new Rfc822Tokenizer();
        String comma = ",";
        String space = " ";
        String text = "text";
        assertEquals(text + comma + space, rfc822Tokenizer.terminateToken(text));
        text = null;
        assertEquals(text + comma + space, rfc822Tokenizer.terminateToken(null));
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT ,
        notes = "Broken syntax tolerance is hard to specify",
        method = "tokenize",
        args = {java.lang.CharSequence.class}
    )
    @ToBeFixed(bug = "", explanation = "" +
            "1. the comment doesn't be cleaned before find next token." +
            "2. the javadoc for findTokenEnd() is incomplete." +
            "   - not clear what is supposed result if text is null.")
    public void testTokenize() {
        Rfc822Token[] tokens = Rfc822Tokenizer.tokenize("");
        assertEquals(0, tokens.length);
        String text = "\"Berg\" (home) <berg\\@google.com>, tom\\@google.com (work)";
        tokens = Rfc822Tokenizer.tokenize(text);
        assertEquals(2, tokens.length);
        localAssertEquals(tokens[0], "Berg", "berg\\@google.com", "home");
        localAssertEquals(tokens[1], null, "tom\\@google.com", "work");
        text = "Foo Bar (something) <foo\\@google.com>, blah\\@google.com (something)";
        tokens = Rfc822Tokenizer.tokenize(text);
        assertEquals(2, tokens.length);
        localAssertEquals(tokens[0], "Foo Bar", "foo\\@google.com", "something");
        localAssertEquals(tokens[1], null, "blah\\@google.com", "something");
        try {
            Rfc822Tokenizer.tokenize(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    private void localAssertEquals(Rfc822Token token, String name,
            String address, String comment) {
        assertEquals(name, token.getName());
        assertEquals(address, token.getAddress());
        assertEquals(comment, token.getComment());
    }
}

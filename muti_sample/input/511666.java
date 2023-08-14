public class JSONTokenerTest extends TestCase {
    public void testNulls() throws JSONException {
        new JSONTokener(null).back();
        try {
            new JSONTokener(null).more();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).next();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).next(3);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).next('A');
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).nextClean();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).nextString('"');
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).nextTo('A');
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).nextTo("ABC");
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).nextValue();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).skipPast("ABC");
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new JSONTokener(null).skipTo('A');
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals("foo! at character 0 of null",
                new JSONTokener(null).syntaxError("foo!").getMessage());
        assertEquals(" at character 0 of null", new JSONTokener(null).toString());
    }
    public void testEmptyString() throws JSONException {
        JSONTokener backTokener = new JSONTokener("");
        backTokener.back();
        assertEquals(" at character 0 of ", backTokener.toString());
        assertFalse(new JSONTokener("").more());
        assertEquals('\0', new JSONTokener("").next());
        try {
            new JSONTokener("").next(3);
            fail();
        } catch (JSONException expected) {
        }
        try {
            new JSONTokener("").next('A');
            fail();
        } catch (JSONException e) {
        }
        assertEquals('\0', new JSONTokener("").nextClean());
        try {
            new JSONTokener("").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        assertEquals("", new JSONTokener("").nextTo('A'));
        assertEquals("", new JSONTokener("").nextTo("ABC"));
        try {
            new JSONTokener("").nextValue();
            fail();
        } catch (JSONException e) {
        }
        new JSONTokener("").skipPast("ABC");
        assertEquals('\0', new JSONTokener("").skipTo('A'));
        assertEquals("foo! at character 0 of ",
                new JSONTokener("").syntaxError("foo!").getMessage());
        assertEquals(" at character 0 of ", new JSONTokener("").toString());
    }
    public void testCharacterNavigation() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener("ABCDE");
        assertEquals('A', abcdeTokener.next());
        assertEquals('B', abcdeTokener.next('B'));
        assertEquals("CD", abcdeTokener.next(2));
        try {
            abcdeTokener.next(2);
            fail();
        } catch (JSONException e) {
        }
        assertEquals('E', abcdeTokener.nextClean());
        assertEquals('\0', abcdeTokener.next());
        assertFalse(abcdeTokener.more());
        abcdeTokener.back();
        assertTrue(abcdeTokener.more());
        assertEquals('E', abcdeTokener.next());
    }
    public void testBackNextAndMore() throws JSONException {
        JSONTokener abcTokener = new JSONTokener("ABC");
        assertTrue(abcTokener.more());
        abcTokener.next();
        abcTokener.next();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        abcTokener.back();
        abcTokener.back();
        abcTokener.back(); 
        assertEquals('A', abcTokener.next());
    }
    public void testNextMatching() throws JSONException {
        JSONTokener abcdTokener = new JSONTokener("ABCD");
        assertEquals('A', abcdTokener.next('A'));
        try {
            abcdTokener.next('C'); 
            fail();
        } catch (JSONException e) {
        }
        assertEquals('C', abcdTokener.next('C'));
        assertEquals('D', abcdTokener.next('D'));
        try {
            abcdTokener.next('E');
            fail();
        } catch (JSONException e) {
        }
    }
    public void testNextN() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener("ABCDEF");
        assertEquals("", abcdeTokener.next(0));
        try {
            abcdeTokener.next(7);
            fail();
        } catch (JSONException e) {
        }
        assertEquals("ABC", abcdeTokener.next(3));
        try {
            abcdeTokener.next(4);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testNextNWithAllRemaining() throws JSONException {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.next(3);
        try {
            tokener.next(3);
        } catch (JSONException e) {
            AssertionFailedError error = new AssertionFailedError("off-by-one error?");
            error.initCause(e);
            throw error;
        }
    }
    public void testNext0() throws JSONException {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.next(5);
        tokener.next();
        try {
            tokener.next(0);
        } catch (JSONException e) {
            Error error = new AssertionFailedError("Returning an empty string should be valid");
            error.initCause(e);
            throw error;
        }
    }
    public void testNextCleanComments() throws JSONException {
        JSONTokener tokener = new JSONTokener(
                "  A  B D */ E");
        assertEquals('A', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('*', tokener.nextClean());
        assertEquals('/', tokener.nextClean());
        assertEquals('E', tokener.nextClean());
    }
    public void testNextCleanHashComments() throws JSONException {
        JSONTokener tokener = new JSONTokener("A # B */  \nD #");
        assertEquals('A', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('\0', tokener.nextClean());
    }
    public void testNextCleanCommentsTrailingSingleSlash() throws JSONException {
        JSONTokener tokener = new JSONTokener(" / S /");
        assertEquals('/', tokener.nextClean());
        assertEquals('S', tokener.nextClean());
        assertEquals('/', tokener.nextClean());
        assertEquals("nextClean doesn't consume a trailing slash",
                '\0', tokener.nextClean());
    }
    public void testNextCleanTrailingOpenComment() throws JSONException {
        try {
            new JSONTokener("  
    public void testNextCleanRetainedWhitespace() throws JSONException {
        assertNotClean("null",                      '\u0000');
        assertNotClean("next line",                 '\u0085');
        assertNotClean("non-breaking space",        '\u00a0');
        assertNotClean("ogham space mark",          '\u1680');
        assertNotClean("mongolian vowel separator", '\u180e');
        assertNotClean("en quad",                   '\u2000');
        assertNotClean("em quad",                   '\u2001');
        assertNotClean("en space",                  '\u2002');
        assertNotClean("em space",                  '\u2003');
        assertNotClean("three-per-em space",        '\u2004');
        assertNotClean("four-per-em space",         '\u2005');
        assertNotClean("six-per-em space",          '\u2006');
        assertNotClean("figure space",              '\u2007');
        assertNotClean("punctuation space",         '\u2008');
        assertNotClean("thin space",                '\u2009');
        assertNotClean("hair space",                '\u200a');
        assertNotClean("zero-width space",          '\u200b');
        assertNotClean("left-to-right mark",        '\u200e');
        assertNotClean("right-to-left mark",        '\u200f');
        assertNotClean("line separator",            '\u2028');
        assertNotClean("paragraph separator",       '\u2029');
        assertNotClean("narrow non-breaking space", '\u202f');
        assertNotClean("medium mathematical space", '\u205f');
        assertNotClean("ideographic space",         '\u3000');
        assertNotClean("line tabulation",           '\u000b');
        assertNotClean("form feed",                 '\u000c');
        assertNotClean("information separator 4",   '\u001c');
        assertNotClean("information separator 3",   '\u001d');
        assertNotClean("information separator 2",   '\u001e');
        assertNotClean("information separator 1",   '\u001f');
    }
    private void assertNotClean(String name, char c) throws JSONException {
        assertEquals("The character " + name + " is not whitespace according to the JSON spec.",
                c, new JSONTokener(new String(new char[] { c, 'A' })).nextClean());
    }
    public void testNextString() throws JSONException {
        assertEquals("", new JSONTokener("'").nextString('\''));
        assertEquals("", new JSONTokener("\"").nextString('\"'));
        assertEquals("ABC", new JSONTokener("ABC'DEF").nextString('\''));
        assertEquals("ABC", new JSONTokener("ABC'''DEF").nextString('\''));
        assertEquals("ABC", new JSONTokener("A\\B\\C'DEF").nextString('\''));
        JSONTokener tokener = new JSONTokener(" 'abc' 'def' \"ghi\"");
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("abc", tokener.nextString('\''));
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("def", tokener.nextString('\''));
        tokener.next();
        assertEquals('"', tokener.next());
        assertEquals("ghi", tokener.nextString('\"'));
        assertFalse(tokener.more());
    }
    public void testNextStringNoDelimiter() throws JSONException {
        try {
            new JSONTokener("").nextString('\'');
            fail();
        } catch (JSONException e) {
        }
        JSONTokener tokener = new JSONTokener(" 'abc");
        tokener.next();
        tokener.next();
        try {
            tokener.next('\'');
            fail();
        } catch (JSONException e) {
        }
    }
    public void testNextStringEscapedQuote() throws JSONException {
        try {
            new JSONTokener("abc\\").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        assertEquals("abc\"def", new JSONTokener("abc\\\"def\"ghi").nextString('"'));
        assertEquals("abc\\def", new JSONTokener("abc\\\\def\"ghi").nextString('"'));
        assertEquals("abc/def", new JSONTokener("abc\\/def\"ghi").nextString('"'));
        assertEquals("abc\bdef", new JSONTokener("abc\\bdef\"ghi").nextString('"'));
        assertEquals("abc\fdef", new JSONTokener("abc\\fdef\"ghi").nextString('"'));
        assertEquals("abc\ndef", new JSONTokener("abc\\ndef\"ghi").nextString('"'));
        assertEquals("abc\rdef", new JSONTokener("abc\\rdef\"ghi").nextString('"'));
        assertEquals("abc\tdef", new JSONTokener("abc\\tdef\"ghi").nextString('"'));
    }
    public void testNextStringUnicodeEscaped() throws JSONException {
        assertEquals("abc def", new JSONTokener("abc\\u0020def\"ghi").nextString('"'));
        assertEquals("abcU0020def", new JSONTokener("abc\\U0020def\"ghi").nextString('"'));
        try {
            new JSONTokener("abc\\u002\"").nextString('"');
            fail();
        } catch (NumberFormatException e) {
        } catch (JSONException e) {
        }
        try {
            new JSONTokener("abc\\u").nextString('"');
            fail();
        } catch (JSONException e) {
        }
        try {
            new JSONTokener("abc\\u    \"").nextString('"');
            fail();
        } catch (NumberFormatException e) {
        }
        assertEquals("abc\"def", new JSONTokener("abc\\u0022def\"ghi").nextString('"'));
        try {
            new JSONTokener("abc\\u000G\"").nextString('"');
            fail();
        } catch (NumberFormatException e) {
        }
    }
    public void testNextStringNonQuote() throws JSONException {
        assertEquals("AB", new JSONTokener("ABC").nextString('C'));
        assertEquals("ABCD", new JSONTokener("AB\\CDC").nextString('C'));
        assertEquals("AB\nC", new JSONTokener("AB\\nCn").nextString('n'));
    }
    public void testNextTo() throws JSONException {
        assertEquals("ABC", new JSONTokener("ABCDEFG").nextTo("DHI"));
        assertEquals("ABCDEF", new JSONTokener("ABCDEF").nextTo(""));
        JSONTokener tokener = new JSONTokener("ABC\rDEF\nGHI\r\nJKL");
        assertEquals("ABC", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals("DEF", tokener.nextTo("M"));
        assertEquals('\n', tokener.next());
        assertEquals("GHI", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals('\n', tokener.next());
        assertEquals("JKL", tokener.nextTo("M"));
        tokener = new JSONTokener("ABCDEFGHI");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('E', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('F', tokener.next());
        assertEquals("GHI", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));
        tokener = new JSONTokener(" \t \fABC \t DEF");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());
        tokener = new JSONTokener(" \t \fABC \n DEF");
        assertEquals("ABC", tokener.nextTo("\n"));
        assertEquals("", tokener.nextTo("\n"));
        tokener = new JSONTokener("");
        try {
            tokener.nextTo(null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testNextToTrimming() {
        assertEquals("ABC", new JSONTokener("\t ABC \tDEF").nextTo("DE"));
        assertEquals("ABC", new JSONTokener("\t ABC \tDEF").nextTo('D'));
    }
    public void testNextToTrailing() {
        assertEquals("ABC DEF", new JSONTokener("\t ABC DEF \t").nextTo("G"));
        assertEquals("ABC DEF", new JSONTokener("\t ABC DEF \t").nextTo('G'));
    }
    public void testNextToDoesntStopOnNull() {
        String message = "nextTo() shouldn't stop after \\0 characters";
        JSONTokener tokener = new JSONTokener(" \0\t \fABC \n DEF");
        assertEquals(message, "ABC", tokener.nextTo("D"));
        assertEquals(message, '\n', tokener.next());
        assertEquals(message, "", tokener.nextTo("D"));
    }
    public void testNextToConsumesNull() {
        String message = "nextTo shouldn't consume \\0.";
        JSONTokener tokener = new JSONTokener("ABC\0DEF");
        assertEquals(message, "ABC", tokener.nextTo("\0"));
        assertEquals(message, '\0', tokener.next());
        assertEquals(message, "DEF", tokener.nextTo("\0"));
    }
    public void testSkipPast() {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("EF");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABCDEF");
        tokener.skipPast("G");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABC\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());
        assertEquals('A', tokener.next());
        tokener = new JSONTokener("\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABC\nDEF");
        tokener.skipPast("DEF");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABC");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());
        tokener = new JSONTokener("ABCDABCDABCD");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener = new JSONTokener("");
        try {
            tokener.skipPast(null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testSkipTo() {
        JSONTokener tokener = new JSONTokener("ABCDEF");
        tokener.skipTo('A');
        assertEquals('A', tokener.next());
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
        tokener.skipTo('G');
        assertEquals('E', tokener.next());
        tokener.skipTo('A');
        assertEquals('F', tokener.next());
        tokener = new JSONTokener("ABC\nDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());
        tokener = new JSONTokener("ABCfDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());
        tokener = new JSONTokener("ABC");
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
    }
    public void testSkipToStopsOnNull() {
        JSONTokener tokener = new JSONTokener("ABC\0DEF");
        tokener.skipTo('F');
        assertEquals("skipTo shouldn't stop when it sees '\\0'", 'F', tokener.next());
    }
    public void testDehexchar() {
        assertEquals( 0, JSONTokener.dehexchar('0'));
        assertEquals( 1, JSONTokener.dehexchar('1'));
        assertEquals( 2, JSONTokener.dehexchar('2'));
        assertEquals( 3, JSONTokener.dehexchar('3'));
        assertEquals( 4, JSONTokener.dehexchar('4'));
        assertEquals( 5, JSONTokener.dehexchar('5'));
        assertEquals( 6, JSONTokener.dehexchar('6'));
        assertEquals( 7, JSONTokener.dehexchar('7'));
        assertEquals( 8, JSONTokener.dehexchar('8'));
        assertEquals( 9, JSONTokener.dehexchar('9'));
        assertEquals(10, JSONTokener.dehexchar('A'));
        assertEquals(11, JSONTokener.dehexchar('B'));
        assertEquals(12, JSONTokener.dehexchar('C'));
        assertEquals(13, JSONTokener.dehexchar('D'));
        assertEquals(14, JSONTokener.dehexchar('E'));
        assertEquals(15, JSONTokener.dehexchar('F'));
        assertEquals(10, JSONTokener.dehexchar('a'));
        assertEquals(11, JSONTokener.dehexchar('b'));
        assertEquals(12, JSONTokener.dehexchar('c'));
        assertEquals(13, JSONTokener.dehexchar('d'));
        assertEquals(14, JSONTokener.dehexchar('e'));
        assertEquals(15, JSONTokener.dehexchar('f'));
        for (int c = 0; c <= 0xFFFF; c++) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                continue;
            }
            assertEquals("dehexchar " + c, -1, JSONTokener.dehexchar((char) c));
        }
    }
}
